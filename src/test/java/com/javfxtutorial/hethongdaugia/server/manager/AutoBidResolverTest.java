package com.javfxtutorial.hethongdaugia.server.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

@DisplayName("Luật chọn người thắng auto-bid")
class AutoBidResolverTest {
    private static final LocalDateTime EARLY = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime LATE = LocalDateTime.of(2026, 1, 1, 10, 1);

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
    @DisplayName("Auto-bid decision table")
    class AutoBidDecisionTable {
        @Test
        @DisplayName("một bot đủ điều kiện đặt giá tối thiểu")
        void singleEligibleBot_bidsMinimumRequired() throws Exception {
            Auction auction = auction("100", "10", "other");
            AutoBidResult result =
                runAutoBid(auction, List.of(bot(1, "alice", "200", true, EARLY)));

            assertBid(result, 1, "110");
            assertEquals("alice", auction.getWinnerName());
            assertEquals(new BigDecimal("110"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("bỏ qua bot đã tắt")
        void inactiveBot_isIgnored() throws Exception {
            Auction auction = auction("100", "10", "other");
            AutoBidResult result =
                runAutoBid(auction, List.of(bot(1, "alice", "200", false, EARLY)));

            assertNoBid(result);
            assertEquals("other", auction.getWinnerName());
            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("bỏ qua bot có maxBid dưới giá tối thiểu")
        void botBelowMinimumRequired_isIgnored() throws Exception {
            Auction auction = auction("100", "10", "other");
            AutoBidResult result =
                runAutoBid(auction, List.of(bot(1, "alice", "109", true, EARLY)));

            assertNoBid(result);
            assertEquals("other", auction.getWinnerName());
            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("bot maxBid cao nhất thắng với giá vừa đủ hơn bot thứ hai")
        void highestMaxBidWinsAndPaysSecondMaxPlusStep() throws Exception {
            Auction auction = auction("100", "10", "carol");
            AutoBidConfig lower = bot(1, "alice", "200", true, EARLY);
            AutoBidConfig higher = bot(2, "bob", "500", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(lower, higher));

            assertBidSequence(result, List.of(expectedBid(1, "200"), expectedBid(2, "210")));
            assertEquals("bob", auction.getWinnerName());
            assertEquals(new BigDecimal("210"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("cùng maxBid thì bot đăng ký sớm thắng")
        void sameMaxBidEarlierRegistrationWinsAndPaysMaxBid() throws Exception {
            Auction auction = auction("100", "10", "carol");
            AutoBidConfig early = bot(1, "alice", "300", true, EARLY);
            AutoBidConfig late = bot(2, "bob", "300", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(late, early));

            assertBid(result, 1, "300");
            assertEquals("alice", auction.getWinnerName());
            assertEquals(new BigDecimal("300"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("người đang dẫn đầu giữ lợi thế khi cùng maxBid")
        void sameMaxBidCurrentWinnerKeepsLeadAndPriceMovesToMaxBid() throws Exception {
            Auction auction = auction("100", "10", "alice");
            AutoBidConfig currentWinner = bot(1, "alice", "300", true, EARLY);
            AutoBidConfig challenger = bot(2, "bob", "300", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(challenger, currentWinner));

            assertBid(result, 1, "300");
            assertEquals("alice", auction.getWinnerName());
            assertEquals(new BigDecimal("300"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("nguoi dang dan dau giu loi the cung maxBid du dang ky muon hon")
        void sameMaxBidCurrentWinnerKeepsLeadEvenWithLaterRegistration() throws Exception {
            Auction auction = auction("100", "10", "alice");
            AutoBidConfig challenger = bot(2, "bob", "300", true, EARLY);
            AutoBidConfig currentWinner = bot(1, "alice", "300", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(currentWinner, challenger));

            assertBid(result, 1, "300");
            assertEquals("alice", auction.getWinnerName());
            assertEquals(new BigDecimal("300"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("giá auto-bid không vượt maxBid của bot thắng")
        void finalAmountIsClampedToWinnerMaxBid() throws Exception {
            Auction auction = auction("100", "10", "carol");
            AutoBidConfig winner = bot(1, "alice", "205", true, EARLY);
            AutoBidConfig second = bot(2, "bob", "200", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(winner, second));

            assertBidSequence(result, List.of(expectedBid(2, "200"), expectedBid(1, "205")));
            assertEquals("alice", auction.getWinnerName());
            assertEquals(new BigDecimal("205"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("bot đang dẫn đầu không tự đấu với chính mình")
        void currentWinnerDoesNotBidAgainstThemself() throws Exception {
            Auction auction = auction("100", "10", "alice");
            AutoBidResult result =
                runAutoBid(auction, List.of(bot(1, "alice", "500", true, EARLY)));

            assertNoBid(result);
            assertEquals("alice", auction.getWinnerName());
            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        }

        @Test
        @DisplayName("bot thấp hơn vẫn thắng khi bot cao hơn bị tắt")
        void lowerMaxBotCanWinWhenHigherBotIsInactive() throws Exception {
            Auction auction = auction("100", "10", "carol");
            AutoBidConfig inactiveHigher = bot(1, "alice", "500", false, EARLY);
            AutoBidConfig activeLower = bot(2, "bob", "200", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(inactiveHigher, activeLower));

            assertBid(result, 2, "110");
            assertEquals("bob", auction.getWinnerName());
            assertEquals(new BigDecimal("110"), auction.getCurrentPrice());
        }
    }

    private AutoBidResult runAutoBid(Auction auction, List<AutoBidConfig> configs)
        throws Exception {
        TestStateSupport.activeAuctions(manager).put(auction.getAuctionId(), auction);
        TestStateSupport.autoBidRegistry(manager)
            .put(auction.getAuctionId(), new ArrayList<>(configs));
        RecordingBidListener listener = new RecordingBidListener();
        manager.registerToAuction(listener, auction.getAuctionId());

        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        BidDAO bidDAO = mock(BidDAO.class);
        ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);
        ArgumentCaptor<BidTransaction> bidCaptor = ArgumentCaptor.forClass(BidTransaction.class);
        when(auctionDAO.update(any(Auction.class))).thenReturn(1);
        when(bidDAO.insertBid(bidCaptor.capture())).thenReturn(true);
        when(participatedAuctionDAO.insert(any(BidTransaction.class))).thenReturn(1);

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
             MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
             MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                 mockParticipatedAuctionDAO(participatedAuctionDAO)) {
            TestStateSupport.executeAutoBidCheck(manager, auction);
        }

        return new AutoBidResult(
                auctionDAO, bidDAO, bidCaptor.getAllValues(), listener.receivedBids);
    }

    private void assertBid(AutoBidResult result, int expectedUserId, String expectedAmount)
        throws DataException {
        assertEquals(1, result.capturedBids().size());
        BidTransaction bid = result.capturedBids().get(0);

        assertEquals(expectedUserId, bid.getBidderId());
        assertEquals(new BigDecimal(expectedAmount), bid.getAmount());
        assertBidSequence(result.notifiedBids(), expectedBid(expectedUserId, expectedAmount));
        verify(result.auctionDAO()).update(any(Auction.class));
        verify(result.bidDAO()).insertBid(any(BidTransaction.class));
    }

    private void assertBidSequence(AutoBidResult result, List<ExpectedBid> expectedBids)
        throws DataException {
        assertEquals(expectedBids.size(), result.capturedBids().size());
        assertBidSequence(result.capturedBids(), expectedBids.toArray(ExpectedBid[]::new));
        assertBidSequence(result.notifiedBids(), expectedBids.toArray(ExpectedBid[]::new));
        verify(result.auctionDAO()).update(any(Auction.class));
        verify(result.bidDAO(), times(expectedBids.size())).insertBid(any(BidTransaction.class));
    }

    private void assertBidSequence(List<BidTransaction> actualBids, ExpectedBid... expectedBids) {
        assertEquals(expectedBids.length, actualBids.size());
        for (int i = 0; i < expectedBids.length; i++) {
            ExpectedBid expected = expectedBids[i];
            BidTransaction actual = actualBids.get(i);
            assertEquals(expected.userId(), actual.getBidderId());
            assertEquals(new BigDecimal(expected.amount()), actual.getAmount());
        }
    }

    private void assertNoBid(AutoBidResult result) throws DataException {
        assertTrue(result.capturedBids().isEmpty());
        assertTrue(result.notifiedBids().isEmpty());
        verify(result.auctionDAO(), never()).update(any(Auction.class));
        verify(result.bidDAO(), never()).insertBid(any(BidTransaction.class));
    }

    private MockedStatic<AuctionDAO> mockAuctionDAO(AuctionDAO auctionDAO) {
        MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class);
        mockedAuctionDAO.when(AuctionDAO::getInstance).thenReturn(auctionDAO);
        return mockedAuctionDAO;
    }

    private MockedStatic<BidDAO> mockBidDAO(BidDAO bidDAO) {
        MockedStatic<BidDAO> mockedBidDAO = mockStatic(BidDAO.class);
        mockedBidDAO.when(BidDAO::getInstance).thenReturn(bidDAO);
        return mockedBidDAO;
    }

    private MockedStatic<ParticipatedAuctionDAO> mockParticipatedAuctionDAO(
        ParticipatedAuctionDAO participatedAuctionDAO) {
        MockedStatic<ParticipatedAuctionDAO> mockedParticipatedAuctionDAO =
            mockStatic(ParticipatedAuctionDAO.class);
        mockedParticipatedAuctionDAO
            .when(ParticipatedAuctionDAO::getInstance)
            .thenReturn(participatedAuctionDAO);
        return mockedParticipatedAuctionDAO;
    }

    private Auction auction(String currentPrice, String stepPrice, String winnerName) {
        Auction auction = new Auction();
        auction.setAuctionId(100);
        auction.setCurrentPrice(new BigDecimal(currentPrice));
        auction.setStepPrice(new BigDecimal(stepPrice));
        auction.setWinningPrice(new BigDecimal(currentPrice));
        auction.setWinnerName(winnerName);
        auction.setStartingTime(LocalDateTime.now().minusMinutes(5));
        auction.setEndingTime(LocalDateTime.now().plusMinutes(5));
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }

    private AutoBidConfig bot(
        int userId,
        String userName,
        String maxPrice,
        boolean active,
        LocalDateTime registeredAt) {
        AutoBidConfig config =
            new AutoBidConfig(userId, userName, 100, new BigDecimal(maxPrice), active);
        config.setRegisteredAt(registeredAt);
        return config;
    }

    private ExpectedBid expectedBid(int userId, String amount) {
        return new ExpectedBid(userId, amount);
    }

    private record ExpectedBid(int userId, String amount) {}

    private static final class RecordingBidListener implements BidListener {
        private final List<BidTransaction> receivedBids = new ArrayList<>();

        @Override
        public void onPlaceBid(BidTransaction bid, ClientHandler senderThread) {
            receivedBids.add(bid);
        }
    }

    private record AutoBidResult(
        AuctionDAO auctionDAO,
        BidDAO bidDAO,
        List<BidTransaction> capturedBids,
        List<BidTransaction> notifiedBids) {}
}
