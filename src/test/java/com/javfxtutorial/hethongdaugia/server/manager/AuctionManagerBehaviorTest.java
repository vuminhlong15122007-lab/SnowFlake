package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.server.network.BidListener;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionManagerBehaviorTest {
    private AuctionManager manager;

    @BeforeEach
    void setUp() throws Exception {
        manager = AuctionManager.getInstance();
        TestStateSupport.resetAuctionManager(manager);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestStateSupport.resetAuctionManager(manager);
    }

    @Nested
    @DisplayName("Bid validation")
    class BidValidation {
        @Test
        void checkValidBid_acceptsExactlyCurrentPlusStep() {
            Auction auction = auctionWithPrice("100", "10");
            assertTrue(manager.checkValidBid(auction, new BigDecimal("110")));
        }

        @Test
        void checkValidBid_acceptsGreaterThanCurrentPlusStep() {
            Auction auction = auctionWithPrice("100", "10");
            assertTrue(manager.checkValidBid(auction, new BigDecimal("1000")));
        }

        @Test
        void checkValidBid_rejectsAmountsBelowMinimum() {
            Auction auction = auctionWithPrice("100", "10");

            assertFalse(manager.checkValidBid(auction, new BigDecimal("109.99")));
            assertFalse(manager.checkValidBid(auction, new BigDecimal("100")));
            assertFalse(manager.checkValidBid(auction, new BigDecimal("0")));
            assertFalse(manager.checkValidBid(auction, new BigDecimal("-1")));
        }
    }

    @Nested
    @DisplayName("Auction status")
    class AuctionStatusRefresh {
        @Test
        void refreshAuctionStatus_keepsNotStartBeforeStart_withoutDatabaseUpdate() throws DataException {
            Auction auction = auctionWithTime(
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2),
                    AuctionStatus.NOT_START
            );

            assertEquals(AuctionStatus.NOT_START, manager.refreshAuctionStatus(auction));
            assertEquals(AuctionStatus.NOT_START, auction.getStatus());
        }

        @Test
        void refreshAuctionStatus_keepsRunningDuringAuction_withoutDatabaseUpdate() throws DataException {
            Auction auction = auctionWithTime(
                    LocalDateTime.now().minusMinutes(5),
                    LocalDateTime.now().plusMinutes(5),
                    AuctionStatus.RUNNING
            );

            assertEquals(AuctionStatus.RUNNING, manager.refreshAuctionStatus(auction));
            assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        }

        @Test
        void refreshAuctionStatus_keepsClosedAfterEnd_withoutDatabaseUpdate() throws DataException {
            Auction auction = auctionWithTime(
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1),
                    AuctionStatus.CLOSED
            );

            assertEquals(AuctionStatus.CLOSED, manager.refreshAuctionStatus(auction));
            assertEquals(AuctionStatus.CLOSED, auction.getStatus());
        }

        @Test
        void checkPaymentStatus_keepsPaidAuctionAfterPaymentWindow() throws DataException {
            Auction auction = auctionWithTime(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusHours(25),
                    AuctionStatus.PAID
            );

            assertEquals(AuctionStatus.PAID, manager.checkPaymentStatus(auction));
        }

        @Test
        void checkPaymentStatus_keepsStatusBeforePaymentWindowExpires() throws DataException {
            Auction auction = auctionWithTime(
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1),
                    AuctionStatus.CLOSED
            );

            assertEquals(AuctionStatus.CLOSED, manager.checkPaymentStatus(auction));
        }
    }

    @Nested
    @DisplayName("Observer registry")
    class ObserverRegistry {
        @Test
        void registerToAuction_addsListenerOnceOnly() throws Exception {
            RecordingBidListener listener = new RecordingBidListener();

            manager.registerToAuction(listener, 100);
            manager.registerToAuction(listener, 100);

            Map<Integer, List<BidListener>> subscribers = TestStateSupport.auctionSubscribers(manager);
            assertEquals(1, subscribers.get(100).size());
            assertSame(listener, subscribers.get(100).get(0));
        }

        @Test
        void unregisterFromAuction_removesExistingListener() throws Exception {
            RecordingBidListener listener = new RecordingBidListener();
            manager.registerToAuction(listener, 100);

            manager.unregisterFromAuction(listener, 100);

            assertTrue(TestStateSupport.auctionSubscribers(manager).get(100).isEmpty());
        }

        @Test
        void notifySubscribers_deliversBidToEveryRegisteredListener() throws Exception {
            RecordingBidListener first = new RecordingBidListener();
            RecordingBidListener second = new RecordingBidListener();
            BidTransaction bid = bid(100, 2, "150");

            manager.registerToAuction(first, 100);
            manager.registerToAuction(second, 100);

            TestStateSupport.notifySubscribers(manager, 100, bid, null);

            assertEquals(1, first.callCount);
            assertEquals(1, second.callCount);
            assertSame(bid, first.lastBid);
            assertSame(bid, second.lastBid);
        }
    }

    @Nested
    @DisplayName("Auto-bid registry")
    class AutoBidRegistry {
        @Test
        void registerAutoBid_inactiveConfigRemovesExistingConfigWithoutDatabaseAccess() throws Exception {
            AutoBidConfig active = new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), true);
            AutoBidConfig inactive = new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), false);

            TestStateSupport.autoBidRegistry(manager).put(100, new java.util.ArrayList<>(List.of(active)));

            assertTrue(manager.registerAutoBid(inactive));

            assertTrue(TestStateSupport.autoBidRegistry(manager).get(100).isEmpty());
        }

        @Test
        void registerAutoBid_replacesPreviousConfigForSameUser() throws Exception {
            Auction auction = auctionWithPrice("100", "10");
            auction.setAuctionId(100);
            auction.setWinnerId(1);
            TestStateSupport.activeAuctions(manager).put(100, auction);

            AutoBidConfig first = new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), true);
            AutoBidConfig second = new AutoBidConfig(1, "alice", 100, new BigDecimal("300"), true);

            assertTrue(manager.registerAutoBid(first));
            assertTrue(manager.registerAutoBid(second));

            List<AutoBidConfig> configs = TestStateSupport.autoBidRegistry(manager).get(100);
            assertEquals(1, configs.size());
            assertEquals(new BigDecimal("300"), configs.get(0).getMaxPrice());
        }

        @Test
        void executeAutoBidCheck_noEligibleBotsLeavesAuctionUnchanged() throws Exception {
            Auction auction = auctionWithPrice("100", "10");
            auction.setAuctionId(100);
            auction.setWinnerId(2);
            TestStateSupport.autoBidRegistry(manager).put(100, new java.util.ArrayList<>(List.of(
                    new AutoBidConfig(1, "alice", 100, new BigDecimal("109"), true),
                    new AutoBidConfig(3, "carol", 100, new BigDecimal("50"), true)
            )));

            TestStateSupport.executeAutoBidCheck(manager, auction);

            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
            assertEquals(2, auction.getWinnerId());
        }

        @Test
        void executeAutoBidCheck_winnerAlreadyLeadingDoesNotCreateNewBid() throws Exception {
            Auction auction = auctionWithPrice("100", "10");
            auction.setAuctionId(100);
            auction.setWinnerId(1);
            TestStateSupport.autoBidRegistry(manager).put(100, new java.util.ArrayList<>(List.of(
                    new AutoBidConfig(1, "alice", 100, new BigDecimal("500"), true)
            )));

            TestStateSupport.executeAutoBidCheck(manager, auction);

            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
            assertEquals(1, auction.getWinnerId());
        }
    }

    private static Auction auctionWithPrice(String currentPrice, String stepPrice) {
        Auction auction = new Auction();
        auction.setAuctionId(100);
        auction.setItem(new Item("seller", 1, 10, "item", "desc", "img", ItemCategory.OTHER));
        auction.setSellerId(1);
        auction.setCurrentPrice(new BigDecimal(currentPrice));
        auction.setInitPrice(new BigDecimal(currentPrice));
        auction.setStepPrice(new BigDecimal(stepPrice));
        auction.setWinningPrice(new BigDecimal(currentPrice));
        auction.setStartingTime(LocalDateTime.now().minusMinutes(5));
        auction.setEndingTime(LocalDateTime.now().plusMinutes(5));
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }

    private static Auction auctionWithTime(LocalDateTime start, LocalDateTime end, AuctionStatus status) {
        Auction auction = auctionWithPrice("100", "10");
        auction.setStartingTime(start);
        auction.setEndingTime(end);
        auction.setStatus(status);
        return auction;
    }

    private static BidTransaction bid(int auctionId, int bidderId, String amount) {
        BidTransaction bid = new BidTransaction();
        bid.setAuctionId(auctionId);
        bid.setBidderId(bidderId);
        bid.setBidderName("bidder-" + bidderId);
        bid.setAmount(new BigDecimal(amount));
        bid.setTimestamp(LocalDateTime.now());
        return bid;
    }

    private static final class RecordingBidListener implements BidListener {
        private int callCount;
        private BidTransaction lastBid;

        @Override
        public void onPlaceBid(BidTransaction bid, ClientHandler senderThread) {
            callCount++;
            lastBid = bid;
        }
    }
}
