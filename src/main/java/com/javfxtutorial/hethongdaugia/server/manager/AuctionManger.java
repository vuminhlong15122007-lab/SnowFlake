package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManger {

    private static AuctionManger instance;

    private AuctionManger() {
    }

    public static AuctionManger getInstance() {
        if (instance == null) {
            instance = new AuctionManger();
        }
        return instance;
    }

    public Auction getCurrentAuction(int auctionId) {
        return AuctionDAO.getInstance().selectById(auctionId);
    }

    private Map<Integer, List<AutoBidConfig>> autoBidRegistry = new ConcurrentHashMap<>();

    public synchronized boolean checkValidBid(Auction currentAuction, double amount) {
        double currentPrice = currentAuction.getCurrentPrice();
        if (amount >= currentPrice + currentAuction.getStepPrice()) {
            return true;
        }
        return false;
    }

    public AuctionStatus refreshAuctionStatus(Auction auction) {
        AuctionStatus previousStatus = auction.getStatus();
        if (LocalDateTime.now().isBefore(auction.getStartingTime())) {
            auction.setStatus(AuctionStatus.NOT_START);
        } else if (LocalDateTime.now().isAfter(auction.getEndingTime())) {
            auction.setStatus(AuctionStatus.CLOSED);
        } else {
            auction.setStatus(AuctionStatus.RUNNING);
        }
        if (previousStatus != auction.getStatus()) {
            AuctionDAO.getInstance().update(auction);
        }
        return auction.getStatus();
    }

    private void checkAndExecuteAutoBids(Auction auction) {
        List<AutoBidConfig> configs = autoBidRegistry.get(auction.getAuctionId());
        if (configs == null || configs.isEmpty()) return;

        // 1. Lấy danh sách các bot đủ điều kiện (MaxPrice >= Giá hiện tại + Bước giá)
        double step = auction.getStepPrice();
        double minRequired = auction.getCurrentPrice() + step;

        List<AutoBidConfig> eligibleBots = new ArrayList<>();
        for (AutoBidConfig c : configs) {
            if (c.isActive() && c.getMaxPrice() >= minRequired) {
                eligibleBots.add(c);
            }
        }

        if (eligibleBots.isEmpty()) return;

        // 2. Sắp xếp các bot theo MaxPrice giảm dần để tìm người cao nhất và thứ hai
        eligibleBots.sort((b1, b2) -> Double.compare(b2.getMaxPrice(), b1.getMaxPrice()));

        AutoBidConfig winnerBot = eligibleBots.get(0);

        // Nếu người đang thắng hiện tại đã là bot có MaxPrice cao nhất thì không cần đè thêm
        if (winnerBot.getUserId() == auction.getWinnerId()) return;

        double finalAmount;
        if (eligibleBots.size() == 1) {
            // Trường hợp 1: Chỉ có 1 bot đủ điều kiện vượt qua giá hiện tại
            // Giá mới = Giá hiện tại + 1 lần bước giá
            finalAmount = minRequired;
        } else {
            // Trường hợp 2: Có ít nhất 2 bot đang "đấu" nhau
            // Giá mới = MaxPrice của người thứ hai + bước giá
            double secondMax = eligibleBots.get(1).getMaxPrice();
            finalAmount = secondMax + step;

            // Đảm bảo giá mới không vượt quá giới hạn (MaxPrice) của người thắng
            if (finalAmount > winnerBot.getMaxPrice()) {
                finalAmount = winnerBot.getMaxPrice();
            }

            // Đảm bảo giá mới vẫn phải cao hơn mức tối thiểu hiện tại
            if (finalAmount < minRequired) {
                finalAmount = minRequired;
            }
        }

        // 3. Thực hiện đặt giá tự động cho winnerBot
        BidTransaction autoBid = new BidTransaction();
        autoBid.setBidderId(winnerBot.getUserId());
        autoBid.setAuctionId(auction.getAuctionId());
        autoBid.setAmount(finalAmount);
        autoBid.setTimestamp(java.time.LocalDateTime.now());
        autoBid.setBidderName(winnerBot.getUserName());

        // Gọi lại placeBid để cập nhật DB và Broadcast cho mọi người
        this.placeBid(auction, autoBid);
    }

    // 1. Quản lý các phiên đấu giá đang diễn ra trong RAM để đảm bảo tất cả dùng chung 1 đối tượng
    private Map<Integer, Auction> activeAuctions = new java.util.concurrent.ConcurrentHashMap<>();
    // Phương thức để nạp Auction vào RAM khi phiên đấu giá bắt đầu
    public void addActiveAuction(Auction auction) {
        activeAuctions.put(auction.getAuctionId(), auction);
    }

    public synchronized boolean registerAutoBid(AutoBidConfig config) {
        List<AutoBidConfig> configs = autoBidRegistry.computeIfAbsent(
                config.getAuctionId(),
                k -> java.util.Collections.synchronizedList(new ArrayList<>())
        );

        synchronized (configs) {
            configs.removeIf(c -> c.getUserId() == config.getUserId());

            if (config.isActive()) {
                configs.add(config);

                // 2. LẤY TỪ RAM: Đảm bảo Bot đấu trên đúng đối tượng mà mọi người đang xem
                Auction current = activeAuctions.get(config.getAuctionId());

                // Nếu chưa có trong RAM, lúc này mới nạp từ DAO (chỉ nạp 1 lần duy nhất)
                if (current == null) {
                    current = AuctionDAO.getInstance().selectById(config.getAuctionId());
                    if (current != null) activeAuctions.put(current.getAuctionId(), current);
                }

                if (current != null) {
                    checkAndExecuteAutoBids(current);
                }
            }
        }
        return true;
    }

//
public synchronized boolean placeBid(Auction auctionFromClient, BidTransaction bid) {
    Auction auction = activeAuctions.get(bid.getAuctionId());
    if (auction == null) return false;

    if (checkValidBid(auction, bid.getAmount())) {
        auction.setCurrentPrice(bid.getAmount());
        auction.setWinnerId(bid.getBidderId());

        // CHỈ LƯU Ở ĐÂY: Đảm bảo dữ liệu nhất quán
        com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO.getInstance().update(auction);
        com.javfxtutorial.hethongdaugia.server.dao.BidDAO.getInstance().insertBid(bid);

        // Broadcast cho mọi người
        Response rp = new Response(true, "Giá mới", bid, new PlaceBidCommand());
        ClientHandler.broadcast(rp, null);

        // Kích hoạt Bot
        checkAndExecuteAutoBids(auction);
        return true;
    }
    return false;
}
}