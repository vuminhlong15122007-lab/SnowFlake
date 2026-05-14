package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ParticipatedAuctionDAO;
import com.javfxtutorial.hethongdaugia.server.network.BidListener;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
    //Néue có người đắtj giá X s cuối thì ra hạn thêm Y
    private static final long ANTI_SNIPE_X_SECONDS = 60;
    private static final long ANTI_SNIPE_Y_SECONDS = 60;

    // ── Singleton thread-safe ─────────────────────────
    private static volatile AuctionManager instance;

    private AuctionManager() {
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) { // double-check
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // ── Observer: auctionId → list listener ──────────
    private final Map<Integer, List<BidListener>> auctionSubscribers
            = new ConcurrentHashMap<>();

    // ── RAM cache ────────────────────────────────────
    private final Map<Integer, Auction> activeAuctions
            = new ConcurrentHashMap<>();

    // ── AutoBid registry ─────────────────────────────
    private final Map<Integer, List<AutoBidConfig>> autoBidRegistry
            = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────
    // SUBSCRIBE / UNSUBSCRIBE
    // ─────────────────────────────────────────────────

    public void registerToAuction(BidListener listener, int auctionId) {
        auctionSubscribers.computeIfAbsent(
                auctionId,
                k -> new CopyOnWriteArrayList<>()
        );

        List<BidListener> list = auctionSubscribers.get(auctionId);
        if (!list.contains(listener)) {
            list.add(listener);
        }

        System.out.println("Đã thêm " + listener + " vào phòng auction id: " + auctionId);
    }

    public void unregisterFromAuction(BidListener listener, int auctionId) {
        List<BidListener> list = auctionSubscribers.get(auctionId);
        if (list != null) list.remove(listener);
        System.out.println("Client hủy đăng ký auction #" + auctionId);
    }

    // ─────────────────────────────────────────────────
    // PLACE BID — logic chính
    // ─────────────────────────────────────────────────
    public void unregisterListenerFromAll(BidListener listener) {
        if (listener == null) return;
        auctionSubscribers.values().forEach(list -> list.remove(listener));
    }

    public synchronized boolean placeBid(BidTransaction bid,
                                         ClientHandler senderThread) {
        if (bid == null || bid.getAmount() == null) {
            return false;
        }

        // 1. Lấy auction từ RAM
        Auction auction = activeAuctions.get(bid.getAuctionId());

        // 2. Nếu RAM trống → nạp từ DB
        if (auction == null) {
            auction = AuctionDAO.getInstance().selectById(bid.getAuctionId());
            if (auction == null) return false; // auction không tồn tại
            activeAuctions.put(auction.getAuctionId(), auction);
        }
        System.out.println("Đã lấy xong auction từ bidAuctionId");

        // 3. Kiểm tra hợp lệ
        AuctionStatus status = refreshAuctionStatus(auction);
        if (status != AuctionStatus.RUNNING) {
            System.out.println("Phiên đấu giá đang trong trạng thái RUNNING");
            return false;
        }

        if (!checkValidBid(auction, bid.getAmount())) {
            System.out.println("Giá không hợp lệ");
            return false; // giá không hợp lệ
        }
        System.out.println("Bid hợp lệ");

        // 4. Cập nhật auction trong RAM
        auction.setCurrentPrice(bid.getAmount());
        auction.setWinnerId(bid.getBidderId());
        auction.setWinningPrice(bid.getAmount());

        System.out.println("Đã cập nhật lại auction");

        // Logic gia hạn phiên đấu giá
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endingTime = auction.getEndingTime();
        long secondsLeft = Duration.between(now, endingTime).toSeconds();
        LocalDateTime endTimeNew;
        if (secondsLeft <= ANTI_SNIPE_X_SECONDS && secondsLeft > 0) {
            endTimeNew = endingTime.plusSeconds(ANTI_SNIPE_Y_SECONDS);
            auction.setEndingTime(endTimeNew);
            System.out.println("GIA HẠN PHIÊN ĐẤU GIÁ THÀNH CÔNG");
        } else {
            endTimeNew = endingTime;
        }

        // 5. Lưu DB
        AuctionDAO.getInstance().update(auction);
        BidDAO.getInstance().insertBid(bid);
        ParticipatedAuctionDAO.getInstance().insert(bid);
        System.out.println("Đã lưu vào database");

        // 6. Thông báo cho tất cả subscriber của auction này
        bid.setNewEndingTime(endTimeNew);
        notifySubscribers(bid.getAuctionId(), bid, senderThread);

        // 7. Kích hoạt AutoBid nếu có
        checkAndExecuteAutoBids(auction);
        return true;
    }

    // ─────────────────────────────────────────────────
    // NOTIFY — thông báo cho tất cả subscriber
    // ─────────────────────────────────────────────────
    private void notifySubscribers(int auctionId,
                                   BidTransaction bid,
                                   ClientHandler sender) {
        List<BidListener> list = auctionSubscribers.get(auctionId);
        if (list == null || list.isEmpty()) return;
        list.forEach(listener -> listener.onPlaceBid(bid, sender));
    }


    // ─────────────────────────────────────────────────
    // CHECK VALID BID
    // ─────────────────────────────────────────────────
    public boolean checkValidBid(Auction auction, BigDecimal amount) {
        return amount.compareTo(auction.getCurrentPrice().add(auction.getStepPrice())) >= 0;
    }

    // ─────────────────────────────────────────────────
    // AUCTION STATUS
    // ─────────────────────────────────────────────────
    public AuctionStatus refreshAuctionStatus(Auction auction) {
        AuctionStatus previousStatus = auction.getStatus();

        if (LocalDateTime.now().isBefore(auction.getStartingTime())) {
            auction.setStatus(AuctionStatus.NOT_START);
        }else if (previousStatus == AuctionStatus.PAID){
           return previousStatus;
        } else if (LocalDateTime.now().isAfter(auction.getEndingTime().plusHours(24))) {
            return checkPaymentStatus(auction);
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

    // ─────────────────────────────────────────────────
    // AUTO BID — giữ nguyên logic của người khác viết
    // ─────────────────────────────────────────────────
    public synchronized boolean registerAutoBid(AutoBidConfig config) {
        config.setRegisteredAt(LocalDateTime.now());
        List<AutoBidConfig> configs = autoBidRegistry.computeIfAbsent(
                config.getAuctionId(),
                k -> Collections.synchronizedList(new ArrayList<>())
        );

        synchronized (configs) {
            configs.removeIf(c -> c.getUserId() == config.getUserId());

            if (config.isActive()) {
                configs.add(config);

                Auction current = activeAuctions.get(config.getAuctionId());
                if (current == null) {
                    current = AuctionDAO.getInstance()
                            .selectById(config.getAuctionId());
                    if (current != null) {
                        activeAuctions.put(current.getAuctionId(), current);
                    }
                }

                if (current != null) {
                    checkAndExecuteAutoBids(current);
                }
            }
        }
        return true;
    }

    private void checkAndExecuteAutoBids(Auction auction) {
        List<AutoBidConfig> configs = autoBidRegistry
                .get(auction.getAuctionId());
        if (configs == null || configs.isEmpty()) return;

        BigDecimal step = auction.getStepPrice(); // lấy giá step
        BigDecimal minRequired = auction.getCurrentPrice().add(step);

        List<AutoBidConfig> eligibleBots = new ArrayList<>();
        for (AutoBidConfig c : configs) {
            if (c.isActive() && c.getMaxPrice().compareTo(minRequired) >= 0) {
                eligibleBots.add(c);
            }
        }
        if (eligibleBots.isEmpty()) return;

        eligibleBots.sort((b1, b2) -> {
            int cmp = b2.getMaxPrice().compareTo(b1.getMaxPrice());
            if (cmp != 0) return cmp;

            return b1.getRegisteredAt().compareTo(b2.getRegisteredAt());
        });

        AutoBidConfig winnerBot = eligibleBots.getFirst();

        // logic của autobid đây hehe
//        Nếu max cao nhất > max cao thứ hai:
//        giá thắng = min(max cao thứ hai + step, max cao nhất)
//
//        Nếu max cao nhất == max cao thứ hai:
//        người đăng ký trước thắng
//        giá thắng = maxBid đó

        BigDecimal finalAmount;

        if (eligibleBots.size() == 1) {
            if (winnerBot.getUserId() == auction.getWinnerId()) return;
            finalAmount = minRequired;
        } else {
            AutoBidConfig secondBot = eligibleBots.get(1);
            BigDecimal secondMax = secondBot.getMaxPrice();

            if (winnerBot.getMaxPrice().compareTo(secondMax) == 0) {
                // Hai bot cùng maxBid: người đăng ký trước thắng ở đúng maxBid
                finalAmount = winnerBot.getMaxPrice();
            } else {
                // Bot thắng chỉ cần hơn bot thứ hai một bước giá,
                // nhưng không được vượt maxBid của chính nó
                finalAmount = secondMax.add(step);

                if (finalAmount.compareTo(winnerBot.getMaxPrice()) > 0) {
                    finalAmount = winnerBot.getMaxPrice();
                }
            }

            if (finalAmount.compareTo(minRequired) < 0) {
                return;
            }
        }

        BidTransaction autoBid = new BidTransaction();
        autoBid.setBidderId(winnerBot.getUserId());
        autoBid.setAuctionId(auction.getAuctionId());
        autoBid.setAmount(finalAmount);
        autoBid.setTimestamp(LocalDateTime.now());
        autoBid.setBidderName(winnerBot.getUserName());

        // Gọi lại placeBid — dùng sender = null vì là bot
        this.placeBid(autoBid, null);
    }

    public List<Auction> getParticipatedAuctionsByBidder(int userId) {
        ArrayList<Auction> auctionList = new ArrayList<>();
        auctionList = (ArrayList<Auction>) ParticipatedAuctionDAO.getInstance().getParticipatedAuctionsByBidder(userId);
        return auctionList;
    }

    public AuctionStatus checkPaymentStatus(Auction auction) {
        if (LocalDateTime.now().isAfter(auction.getEndingTime().plusHours(24))) {
            if (auction.getStatus() != AuctionStatus.PAID) {
                auction.setStatus(AuctionStatus.CANCELLED);
                AuctionDAO.getInstance().update(auction);
            }
        }
        return auction.getStatus();
    }
}
