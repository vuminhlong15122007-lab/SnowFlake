package com.javfxtutorial.hethongdaugia.server.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.network.BidListener;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("Hành vi lõi của AuctionManager")
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
        @DisplayName("chấp nhận giá bằng current + step")
        void checkValidBid_acceptsExactlyCurrentPlusStep() {
            Auction auction = auctionWithPrice("100", "10");
            assertTrue(manager.checkValidBid(auction, new BigDecimal("110")));
        }

        @Test
        @DisplayName("chấp nhận giá lớn hơn current + step")
        void checkValidBid_acceptsGreaterThanCurrentPlusStep() {
            Auction auction = auctionWithPrice("100", "10");
            assertTrue(manager.checkValidBid(auction, new BigDecimal("1000")));
        }

        @Test
        @DisplayName("từ chối giá thấp hơn mức tối thiểu")
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
        @DisplayName("giữ NOT_START trước thời điểm bắt đầu")
        void refreshAuctionStatus_keepsNotStartBeforeStart_withoutDatabaseUpdate()
            throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2),
                    AuctionStatus.NOT_START);

            assertEquals(AuctionStatus.NOT_START, manager.refreshAuctionStatus(auction));
            assertEquals(AuctionStatus.NOT_START, auction.getStatus());
        }

        @Test
        @DisplayName("giữ RUNNING trong thời gian đấu giá")
        void refreshAuctionStatus_keepsRunningDuringAuction_withoutDatabaseUpdate()
            throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusMinutes(5),
                    LocalDateTime.now().plusMinutes(5),
                    AuctionStatus.RUNNING);

            assertEquals(AuctionStatus.RUNNING, manager.refreshAuctionStatus(auction));
            assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        }

        @Test
        @DisplayName("chuyển NOT_START sang RUNNING và cập nhật DB khi đến giờ")
        void refreshAuctionStatus_movesNotStartToRunningAndUpdatesDatabase() throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusMinutes(5),
                    LocalDateTime.now().plusMinutes(5),
                    AuctionStatus.NOT_START);
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(auction)).thenReturn(1);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class)) {
                mockedAuctionDAO.when(AuctionDAO::getInstance).thenReturn(auctionDAO);

                assertEquals(AuctionStatus.RUNNING, manager.refreshAuctionStatus(auction));

                assertEquals(AuctionStatus.RUNNING, auction.getStatus());
                verify(auctionDAO).update(auction);
            }
        }

        @Test
        @DisplayName("giữ CLOSED sau khi kết thúc")
        void refreshAuctionStatus_keepsClosedAfterEnd_withoutDatabaseUpdate() throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1),
                    AuctionStatus.CLOSED);

            assertEquals(AuctionStatus.CLOSED, manager.refreshAuctionStatus(auction));
            assertEquals(AuctionStatus.CLOSED, auction.getStatus());
        }

        @Test
        @DisplayName("giu CANCELLED va khong update DB")
        void refreshAuctionStatus_keepsCancelledWithoutDatabaseUpdate() throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusDays(1),
                    AuctionStatus.CANCELLED);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class)) {
                assertEquals(AuctionStatus.CANCELLED, manager.refreshAuctionStatus(auction));
                assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
                mockedAuctionDAO.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("auction tam thoi id 0 khong update DB khi status doi")
        void refreshAuctionStatus_skipsDatabaseUpdateForZeroAuctionId() throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusMinutes(5),
                    LocalDateTime.now().plusMinutes(5),
                    AuctionStatus.NOT_START);
            auction.setAuctionId(0);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class)) {
                assertEquals(AuctionStatus.RUNNING, manager.refreshAuctionStatus(auction));
                assertEquals(AuctionStatus.RUNNING, auction.getStatus());
                mockedAuctionDAO.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("chuyen CLOSED co winner qua han thanh toan sang CANCELLED va cap nhat DB")
        void refreshAuctionStatus_movesUnpaidWinnerPastPaymentWindowToCancelledAndUpdatesDatabase()
            throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusHours(25),
                    AuctionStatus.CLOSED);
            auction.setWinnerName("winner");
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(auction)).thenReturn(1);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class)) {
                mockedAuctionDAO.when(AuctionDAO::getInstance).thenReturn(auctionDAO);

                // Logic hiện tại chỉ hủy phiên quá hạn thanh toán khi đã có người thắng.
                assertEquals(AuctionStatus.CANCELLED, manager.refreshAuctionStatus(auction));

                assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
                verify(auctionDAO).update(auction);
            }
        }

        @Test
        @DisplayName("giu CLOSED qua han thanh toan khi khong co winner")
        void refreshAuctionStatus_keepsClosedPastPaymentWindowWhenAuctionHasNoWinner()
            throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusHours(25),
                    AuctionStatus.CLOSED);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class)) {
                // Phiên không có người thắng không có bước thanh toán cần timeout.
                assertEquals(AuctionStatus.CLOSED, manager.refreshAuctionStatus(auction));

                assertEquals(AuctionStatus.CLOSED, auction.getStatus());
                mockedAuctionDAO.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("giữ PAID sau cửa sổ thanh toán")
        void checkPaymentStatus_keepsPaidAuctionAfterPaymentWindow() throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusHours(25),
                    AuctionStatus.PAID);
            auction.setWinnerName("abc");

            assertEquals(AuctionStatus.PAID, manager.checkPaymentStatus(auction));
        }

        @Test
        @DisplayName("giữ CLOSED khi chưa hết hạn thanh toán")
        void checkPaymentStatus_keepsStatusBeforePaymentWindowExpires() throws DataException {
            Auction auction =
                auctionWithTime(
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1),
                    AuctionStatus.CLOSED);

            assertEquals(AuctionStatus.CLOSED, manager.checkPaymentStatus(auction));
        }
    }

    @Nested
    @DisplayName("Observer registry")
    class ObserverRegistry {
        @Test
        @DisplayName("không đăng ký trùng listener realtime")
        void registerToAuction_addsListenerOnceOnly() throws Exception {
            RecordingBidListener listener = new RecordingBidListener();

            manager.registerToAuction(listener, 100);
            manager.registerToAuction(listener, 100);

            Map<Integer, List<BidListener>> subscribers =
                TestStateSupport.auctionSubscribers(manager);
            assertEquals(1, subscribers.get(100).size());
            assertSame(listener, subscribers.get(100).get(0));
        }

        @Test
        @DisplayName("gỡ listener khỏi phiên đang theo dõi")
        void unregisterFromAuction_removesExistingListener() throws Exception {
            RecordingBidListener listener = new RecordingBidListener();
            manager.registerToAuction(listener, 100);

            manager.unregisterFromAuction(listener, 100);

            assertTrue(TestStateSupport.auctionSubscribers(manager).get(100).isEmpty());
        }

        @Test
        @DisplayName("gửi bid mới tới tất cả listener của phiên")
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
        @DisplayName("tắt auto-bid xóa cấu hình hiện có")
        void registerAutoBid_inactiveConfigRemovesExistingConfigWithoutDatabaseAccess()
            throws Exception {
            AutoBidConfig active = new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), true);
            AutoBidConfig inactive =
                new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), false);

            TestStateSupport.autoBidRegistry(manager)
                .put(100, new java.util.ArrayList<>(List.of(active)));

            assertTrue(manager.registerAutoBid(inactive));

            assertTrue(TestStateSupport.autoBidRegistry(manager).get(100).isEmpty());
        }

        @Test
        @DisplayName("cấu hình auto-bid mới thay thế cấu hình cũ cùng user")
        void registerAutoBid_replacesPreviousConfigForSameUser() throws Exception {
            Auction auction = auctionWithPrice("100", "10");
            auction.setAuctionId(100);
            auction.setWinnerName("alice");
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
        @DisplayName("không tạo bid khi không có bot đủ điều kiện")
        void executeAutoBidCheck_noEligibleBotsLeavesAuctionUnchanged() throws Exception {
            Auction auction = auctionWithPrice("100", "10");
            auction.setAuctionId(100);
            auction.setWinnerName("bob");
            TestStateSupport.autoBidRegistry(manager)
                .put(
                    100,
                    new java.util.ArrayList<>(
                        List.of(
                            new AutoBidConfig(
                                1, "alice", 100, new BigDecimal("109"), true),
                            new AutoBidConfig(
                                3, "carol", 100, new BigDecimal("50"), true))));

            TestStateSupport.executeAutoBidCheck(manager, auction);

            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
            assertEquals("bob", auction.getWinnerName());
        }

        @Test
        @DisplayName("không tự đẩy giá khi bot đang dẫn đầu")
        void executeAutoBidCheck_winnerAlreadyLeadingDoesNotCreateNewBid() throws Exception {
            Auction auction = auctionWithPrice("100", "10");
            auction.setAuctionId(100);
            auction.setWinnerName("alice");
            TestStateSupport.autoBidRegistry(manager)
                .put(
                    100,
                    new java.util.ArrayList<>(
                        List.of(
                            new AutoBidConfig(
                                1,
                                "alice",
                                100,
                                new BigDecimal("500"),
                                true))));

            TestStateSupport.executeAutoBidCheck(manager, auction);

            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
            assertEquals("alice", auction.getWinnerName());
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

    private static Auction auctionWithTime(
        LocalDateTime start, LocalDateTime end, AuctionStatus status) {
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
