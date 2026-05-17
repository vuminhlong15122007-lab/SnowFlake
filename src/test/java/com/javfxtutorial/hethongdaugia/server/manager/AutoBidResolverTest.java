package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ParticipatedAuctionDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        void singleEligibleBot_bidsMinimumRequired() throws Exception {
            Auction auction = auction("100", "10", 2);
            AutoBidResult result = runAutoBid(auction, List.of(bot(1, "alice", "200", true, EARLY)));

            assertBid(result, 1, "110");
            assertEquals(1, auction.getWinnerId());
            assertEquals(new BigDecimal("110"), auction.getCurrentPrice());
        }

        @Test
        void inactiveBot_isIgnored() throws Exception {
            Auction auction = auction("100", "10", 2);
            AutoBidResult result = runAutoBid(auction, List.of(bot(1, "alice", "200", false, EARLY)));

            assertNoBid(result);
            assertEquals(2, auction.getWinnerId());
            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        }

        @Test
        void botBelowMinimumRequired_isIgnored() throws Exception {
            Auction auction = auction("100", "10", 2);
            AutoBidResult result = runAutoBid(auction, List.of(bot(1, "alice", "109", true, EARLY)));

            assertNoBid(result);
            assertEquals(2, auction.getWinnerId());
            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        }

        @Test
        void highestMaxBidWinsAndPaysSecondMaxPlusStep() throws Exception {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig lower = bot(1, "alice", "200", true, EARLY);
            AutoBidConfig higher = bot(2, "bob", "500", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(lower, higher));

            assertBid(result, 2, "210");
            assertEquals(2, auction.getWinnerId());
            assertEquals(new BigDecimal("210"), auction.getCurrentPrice());
        }

        @Test
        void sameMaxBidEarlierRegistrationWinsAndPaysMaxBid() throws Exception {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig early = bot(1, "alice", "300", true, EARLY);
            AutoBidConfig late = bot(2, "bob", "300", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(late, early));

            assertBid(result, 1, "300");
            assertEquals(1, auction.getWinnerId());
            assertEquals(new BigDecimal("300"), auction.getCurrentPrice());
        }

        @Test
        void sameMaxBidCurrentWinnerKeepsLeadAndPriceMovesToMaxBid() throws Exception {
            Auction auction = auction("100", "10", 1);
            AutoBidConfig currentWinner = bot(1, "alice", "300", true, EARLY);
            AutoBidConfig challenger = bot(2, "bob", "300", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(challenger, currentWinner));

            assertBid(result, 1, "300");
            assertEquals(1, auction.getWinnerId());
            assertEquals(new BigDecimal("300"), auction.getCurrentPrice());
        }

        @Test
        void finalAmountIsClampedToWinnerMaxBid() throws Exception {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig winner = bot(1, "alice", "205", true, EARLY);
            AutoBidConfig second = bot(2, "bob", "200", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(winner, second));

            assertBid(result, 1, "205");
            assertEquals(1, auction.getWinnerId());
            assertEquals(new BigDecimal("205"), auction.getCurrentPrice());
        }

        @Test
        void currentWinnerDoesNotBidAgainstThemself() throws Exception {
            Auction auction = auction("100", "10", 1);
            AutoBidResult result = runAutoBid(auction, List.of(bot(1, "alice", "500", true, EARLY)));

            assertNoBid(result);
            assertEquals(1, auction.getWinnerId());
            assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        }

        @Test
        void lowerMaxBotCanWinWhenHigherBotIsInactive() throws Exception {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig inactiveHigher = bot(1, "alice", "500", false, EARLY);
            AutoBidConfig activeLower = bot(2, "bob", "200", true, LATE);

            AutoBidResult result = runAutoBid(auction, List.of(inactiveHigher, activeLower));

            assertBid(result, 2, "110");
            assertEquals(2, auction.getWinnerId());
            assertEquals(new BigDecimal("110"), auction.getCurrentPrice());
        }
    }

    private AutoBidResult runAutoBid(Auction auction, List<AutoBidConfig> configs) throws Exception {
        TestStateSupport.activeAuctions(manager).put(auction.getAuctionId(), auction);
        TestStateSupport.autoBidRegistry(manager).put(auction.getAuctionId(), new ArrayList<>(configs));

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

        return new AutoBidResult(auctionDAO, bidDAO, bidCaptor.getAllValues());
    }

    private void assertBid(AutoBidResult result, int expectedUserId, String expectedAmount) throws DataException {
        assertEquals(1, result.capturedBids().size());
        BidTransaction bid = result.capturedBids().get(0);

        assertEquals(expectedUserId, bid.getBidderId());
        assertEquals(new BigDecimal(expectedAmount), bid.getAmount());
        verify(result.auctionDAO()).update(any(Auction.class));
        verify(result.bidDAO()).insertBid(any(BidTransaction.class));
    }

    private void assertNoBid(AutoBidResult result) throws DataException {
        assertTrue(result.capturedBids().isEmpty());
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
            ParticipatedAuctionDAO participatedAuctionDAO
    ) {
        MockedStatic<ParticipatedAuctionDAO> mockedParticipatedAuctionDAO =
                mockStatic(ParticipatedAuctionDAO.class);
        mockedParticipatedAuctionDAO.when(ParticipatedAuctionDAO::getInstance)
                .thenReturn(participatedAuctionDAO);
        return mockedParticipatedAuctionDAO;
    }

    private Auction auction(String currentPrice, String stepPrice, int winnerId) {
        Auction auction = new Auction();
        auction.setAuctionId(100);
        auction.setCurrentPrice(new BigDecimal(currentPrice));
        auction.setStepPrice(new BigDecimal(stepPrice));
        auction.setWinningPrice(new BigDecimal(currentPrice));
        auction.setWinnerId(winnerId);
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
            LocalDateTime registeredAt
    ) {
        AutoBidConfig config = new AutoBidConfig(userId, userName, 100, new BigDecimal(maxPrice), active);
        config.setRegisteredAt(registeredAt);
        return config;
    }

    private record AutoBidResult(
            AuctionDAO auctionDAO,
            BidDAO bidDAO,
            List<BidTransaction> capturedBids
    ) {
    }
}
