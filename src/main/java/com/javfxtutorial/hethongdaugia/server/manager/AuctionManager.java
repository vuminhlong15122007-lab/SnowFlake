package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionAlreadyEndedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.BidAmountExceedsLimitException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.domain.AntiSnipeExtender;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
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
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionManager {
    private static final Logger log = LoggerFactory.getLogger(AuctionManager.class);

    private static final long ANTI_SNIPE_X_SECONDS = 60;
    private static final long ANTI_SNIPE_Y_SECONDS = 60;
    
    private final AntiSnipeExtender antiSnipeExtender =
            new AntiSnipeExtender(ANTI_SNIPE_X_SECONDS, ANTI_SNIPE_Y_SECONDS);

    // ── Singleton thread-safe ─────────────────────────
    private static volatile AuctionManager instance;

    private AuctionManager() {}

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
    private final Map<Integer, List<BidListener>> auctionSubscribers = new ConcurrentHashMap<>();

    // ── RAM cache ────────────────────────────────────
    private final Map<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();

    // ── AutoBid registry ─────────────────────────────
    private final Map<Integer, List<AutoBidConfig>> autoBidRegistry = new ConcurrentHashMap<>();
    // ── Lock riêng theo từng auctionId ───────────────
    private final Map<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    private ReentrantLock getAuctionLock(int auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, id -> new ReentrantLock());
    }

    // ─────────────────────────────────────────────────
    // SUBSCRIBE / UNSUBSCRIBE
    // ─────────────────────────────────────────────────

    public void registerToAuction(BidListener listener, int auctionId) {
        if (listener == null) {
            log.warn("Không thể đăng ký auction {} vì listener null", auctionId);
            return;
        }
        auctionSubscribers.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>());

        List<BidListener> list = auctionSubscribers.get(auctionId);
        if (!list.contains(listener)) {
            list.add(listener);
        }

        log.info("Đã thêm {} vào phòng auction id: {}", listener, auctionId);
    }

    public void unregisterFromAuction(BidListener listener, int auctionId) {
        List<BidListener> list = auctionSubscribers.get(auctionId);
        if (list != null) list.remove(listener);
        log.info("Client hủy đăng ký auction #{}", auctionId);
    }

    // ─────────────────────────────────────────────────
    // PLACE BID — logic chính
    // ─────────────────────────────────────────────────
    public void unregisterListenerFromAll(BidListener listener) {
        if (listener == null) return;
        auctionSubscribers.values().forEach(list -> list.remove(listener));
    }

    public boolean placeBid(BidTransaction bid, ClientHandler senderThread)
            throws AuctionNotFoundException, AuctionNotStartedException,
                    AuctionAlreadyEndedException, LowerThanCurrentBidException, SelfBidException,
                    InsufficientIncrementException, DataException, BidAmountExceedsLimitException {

        if (bid == null || bid.getAmount() == null) {return false;}
        ReentrantLock lock = getAuctionLock(bid.getAuctionId());
        lock.lock();
        BidTransaction acceptedBid;
        LocalDateTime endTimeNew;
        try {
            // 1. Lấy auction từ RAM
            Auction auction = activeAuctions.get(bid.getAuctionId());

            // 2. Nếu RAM trống → nạp từ DB
            if (auction == null) {
                auction = AuctionDAO.getInstance().selectById(bid.getAuctionId());
                if (auction == null) {
                    throw new AuctionNotFoundException(bid.getAuctionId());
                } // auction không tồn tại
                activeAuctions.put(auction.getAuctionId(), auction);
            }
            log.info("Đã lấy xong auction từ bidAuctionId");

            // 3. Kiểm tra hợp lệ
            AuctionStatus status = refreshAuctionStatus(auction);
            if (status == AuctionStatus.NOT_START) {
                throw new AuctionNotStartedException(bid.getAuctionId());
            }

            if (status != AuctionStatus.RUNNING) {
                throw new AuctionAlreadyEndedException(bid.getAuctionId());
            }

            BigDecimal minRequired = auction.getCurrentPrice().add(auction.getStepPrice());
            if (bid.getAmount().compareTo(auction.getCurrentPrice()) <= 0) {
                throw new LowerThanCurrentBidException(
                        auction.getCurrentPrice().doubleValue(), bid.getAmount().doubleValue());
            }
            if (bid.getAmount().compareTo(minRequired) < 0) {
                throw new InsufficientIncrementException(
                        auction.getStepPrice().doubleValue(),
                        bid.getAmount().subtract(auction.getCurrentPrice()).doubleValue());
            }
            if (bid.getAmount().compareTo(new BigDecimal("999999999999.99")) > 0) {
                throw new BidAmountExceedsLimitException(
                        999999999999.99, bid.getAmount().doubleValue());
            }
            // KIỂM TRA NGƯỜI BÁN KHÔNG ĐƯỢC ĐẶT GIÁ
            if (bid.getBidderId() == auction.getSellerId()) {
                log.warn("Người bán {} cố gắng đặt giá sản phẩm của chính mình", bid.getBidderId());
                throw new SelfBidException();
            }

            log.info("Bid hợp lệ");

            // 4. Cập nhật auction trong RAM
            auction.setCurrentPrice(bid.getAmount());
            auction.setWinnerId(bid.getBidderId());
            auction.setWinningPrice(bid.getAmount());
            auction.setWinnerName(bid.getBidderName());
            auction.setWinnerEmail(bid.getBidderEmail());
            auction.setWinnerSdt(bid.getBidderSdt());

            log.info("Đã cập nhật lại auction");


            // Logic gia hạn phiên đấu giá
            endTimeNew = antiSnipeExtender.applyIfNeeded(auction);

            // 5. Lưu DB
            AuctionDAO.getInstance().update(auction);
            BidDAO.getInstance().insertBid(bid);
            try {
                ParticipatedAuctionDAO.getInstance().insert(bid);
            } catch (DuplicateKeyException e) {
                log.info(
                        "User {} đã tham gia auction {} rồi, bỏ qua",
                        bid.getBidderId(),
                        bid.getAuctionId());
            }

            log.info("Đã lưu vào database");
            acceptedBid = bid;
            acceptedBid.setNewEndingTime(endTimeNew);

        } finally {
            lock.unlock();
        }

        // 6. Thông báo cho tất cả subscriber của auction này
        notifySubscribers(acceptedBid.getAuctionId(), acceptedBid, senderThread);

        // 7. Kích hoạt AutoBid nếu có
        checkAndExecuteAutoBids(activeAuctions.get(acceptedBid.getAuctionId()));
        return true;
    }

    // ─────────────────────────────────────────────────
    // NOTIFY — thông báo cho tất cả subscriber

    private void notifySubscribers(int auctionId, BidTransaction bid, ClientHandler sender) {
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

    // AUCTION STATUS

    public AuctionStatus refreshAuctionStatus(Auction auction) throws DataException {
        AuctionStatus previousStatus = auction.getStatus();

        if (previousStatus == AuctionStatus.CANCELLED) {
            return previousStatus;
        }
        if (LocalDateTime.now().isBefore(auction.getStartingTime())) {
            auction.setStatus(AuctionStatus.NOT_START);
        } else if (previousStatus == AuctionStatus.PAID) {
            return previousStatus;
        } else if (LocalDateTime.now().isAfter(auction.getEndingTime().plusHours(24))) {
            auction.setStatus(checkPaymentStatus(auction));
        } else if (LocalDateTime.now().isAfter(auction.getEndingTime())) {
            auction.setStatus(AuctionStatus.CLOSED);
        } else {
            auction.setStatus(AuctionStatus.RUNNING);
        }
        if (auction.getAuctionId() == 0) {
            return auction.getStatus();
        }
        if (previousStatus != auction.getStatus()) {
            AuctionDAO.getInstance().update(auction);
        }
        return auction.getStatus();
    }


    public synchronized boolean registerAutoBid(AutoBidConfig config)
            throws DataException,
                    LowerThanCurrentBidException,
                    SelfBidException,
                    InsufficientIncrementException,
                    AuctionNotFoundException,
                    AuctionNotStartedException,
                    AuctionAlreadyEndedException,
                    BidAmountExceedsLimitException {
        config.setRegisteredAt(LocalDateTime.now());
        List<AutoBidConfig> configs =
                autoBidRegistry.computeIfAbsent(
                        config.getAuctionId(),
                        k -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (configs) {
            configs.removeIf(c -> c.getUserId() == config.getUserId());

            if (config.isActive()) {
                configs.add(config);

                Auction current = activeAuctions.get(config.getAuctionId());
                if (current == null) {
                    current = AuctionDAO.getInstance().selectById(config.getAuctionId());
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

    private void checkAndExecuteAutoBids(Auction auction)
            throws LowerThanCurrentBidException,
                    DataException,
                    SelfBidException,
                    InsufficientIncrementException,
                    AuctionNotFoundException,
                    AuctionNotStartedException,
                    AuctionAlreadyEndedException,
                    BidAmountExceedsLimitException {
        List<AutoBidConfig> configs = autoBidRegistry.get(auction.getAuctionId());
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

        eligibleBots.sort(
                (b1, b2) -> {
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

    public List<Auction> getParticipatedAuctionsByBidder(int userId)
            throws QueryExecutionException {
        ArrayList<Auction> auctionList = new ArrayList<>();
        auctionList =
                (ArrayList<Auction>)
                        ParticipatedAuctionDAO.getInstance()
                                .getParticipatedAuctionsByBidder(userId);
        return auctionList;
    }

    public AuctionStatus checkPaymentStatus(Auction auction) throws DataException {
        if (LocalDateTime.now().isAfter(auction.getEndingTime().plusHours(24))) {
            if (auction.getStatus() != AuctionStatus.PAID) {
                auction.setStatus(AuctionStatus.CANCELLED);
            }
        }
        return auction.getStatus();
    }
    public void updateAuctionStatus(int auctionId, AuctionStatus status) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            auction.setStatus(status);
        }
    }
}
