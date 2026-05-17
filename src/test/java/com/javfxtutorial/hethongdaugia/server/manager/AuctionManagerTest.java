package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuctionManagerTest {
    private AuctionManager auctionManager ;
    private Auction auction ;

    @BeforeEach
    void setup() throws Exception {
        auctionManager = AuctionManager.getInstance() ;
        TestStateSupport.resetAuctionManager(auctionManager);
        auction = new Auction();
        auction.setCurrentPrice(new BigDecimal("100.0"));
        auction.setStepPrice(new BigDecimal("10"));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestStateSupport.resetAuctionManager(auctionManager);
    }
    // ─────────────────────────────────────────────────
    // Singleton
    // ─────────────────────────────────────────────────
    @Nested
    @DisplayName("Singleton pattern")
    class SingletonTest {

        @Test
        @DisplayName("getInstance() luôn trả về cùng một instance")
        void singleton_sameInstance() {
            AuctionManager a = AuctionManager.getInstance();
            AuctionManager b = AuctionManager.getInstance();
            assertSame(a, b);
        }

        @Test
        @DisplayName("getInstance() thread-safe: nhiều thread cùng gọi vẫn nhận cùng instance")
        void singleton_threadSafe() throws InterruptedException {
            int threadCount = 20;
            AuctionManager[] results = new AuctionManager[threadCount];
            CountDownLatch latch = new CountDownLatch(1);
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                threads[idx] = new Thread(() -> {
                    try { latch.await(); } catch (InterruptedException ignored) {}
                    results[idx] = AuctionManager.getInstance();
                });
                threads[idx].start();
            }

            latch.countDown();
            for (Thread t : threads) t.join();

            AuctionManager first = results[0];
            for (AuctionManager r : results) {
                assertSame(first, r);
            }
        }
    }

    // ─────────────────────────────────────────────────
    // Anti-Sniping (refreshAuctionStatus không extend thời gian
    // — logic gia hạn chỉ nằm trong placeBid nên test gián tiếp qua flag thời gian)
    // ─────────────────────────────────────────────────
    @Nested
    @DisplayName("Anti-snipe: phiên RUNNING khi còn < 60s")
    class AntiSnipeStatusTest {

        @Test
        @DisplayName("phiên còn 30 giây vẫn là RUNNING (chưa qua endingTime)")
        void stillRunning_within30Seconds() throws DataException {
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(any(Auction.class))).thenReturn(1);
            Auction auction = new Auction();
            auction.setAuctionId(998);
            auction.setStartingTime(LocalDateTime.now().minusHours(1));
            auction.setEndingTime(LocalDateTime.now().plusSeconds(30));
            auction.setStatus(AuctionStatus.NOT_START);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
                AuctionStatus status = auctionManager.refreshAuctionStatus(auction);

                assertEquals(AuctionStatus.RUNNING, status);
                verify(auctionDAO).update(auction);
            }
        }

        @Test
        @DisplayName("phiên còn 0 giây (đúng thời điểm kết thúc) là CLOSED")
        void closed_atEndingTime() throws DataException {
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(any(Auction.class))).thenReturn(1);
            Auction auction = new Auction();
            auction.setAuctionId(997);
            auction.setStartingTime(LocalDateTime.now().minusHours(1));
            auction.setEndingTime(LocalDateTime.now().minusSeconds(1));
            auction.setStatus(AuctionStatus.RUNNING);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
                AuctionStatus status = auctionManager.refreshAuctionStatus(auction);

                assertEquals(AuctionStatus.CLOSED, status);
                verify(auctionDAO).update(auction);
            }
        }
        @Test
        void sameMaxBid_shouldChooseEarlierUser_andPriceEqualsMaxBid() throws Exception {
            Auction auction = new Auction();
            auction.setAuctionId(100);
            auction.setCurrentPrice(new BigDecimal("50"));
            auction.setStepPrice(new BigDecimal("10"));
            auction.setWinningPrice(new BigDecimal("50"));
            auction.setWinnerId(9);
            auction.setStartingTime(LocalDateTime.now().minusMinutes(5));
            auction.setEndingTime(LocalDateTime.now().plusMinutes(5));
            auction.setStatus(AuctionStatus.RUNNING);
            TestStateSupport.activeAuctions(auctionManager).put(100, auction);

            AutoBidConfig early = new AutoBidConfig(1, "A", 100, new BigDecimal("100"), true);
            AutoBidConfig late = new AutoBidConfig(2, "B", 100, new BigDecimal("100"), true);
            early.setRegisteredAt(LocalDateTime.of(2026, 1, 1, 10, 0));
            late.setRegisteredAt(LocalDateTime.of(2026, 1, 1, 10, 1));
            TestStateSupport.autoBidRegistry(auctionManager).put(100, new java.util.ArrayList<>(List.of(late, early)));

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
                TestStateSupport.executeAutoBidCheck(auctionManager, auction);

                assertEquals(1, auction.getWinnerId());
                assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
                assertEquals(1, bidCaptor.getValue().getBidderId());
                assertEquals(new BigDecimal("100"), bidCaptor.getValue().getAmount());
                verify(auctionDAO).update(auction);
            }
        }

        @Test
        void placeBid_rejectsAuctionBeforeStartAndDoesNotPersistBid() throws Exception {
            Auction auction = new Auction();
            auction.setAuctionId(101);
            auction.setCurrentPrice(new BigDecimal("100"));
            auction.setStepPrice(new BigDecimal("10"));
            auction.setWinningPrice(new BigDecimal("100"));
            auction.setStartingTime(LocalDateTime.now().plusMinutes(5));
            auction.setEndingTime(LocalDateTime.now().plusHours(1));
            auction.setStatus(AuctionStatus.NOT_START);
            TestStateSupport.activeAuctions(auctionManager).put(101, auction);

            BidTransaction bid = new BidTransaction();
            bid.setAuctionId(101);
            bid.setBidderId(1);
            bid.setAmount(new BigDecimal("200"));

            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            BidDAO bidDAO = mock(BidDAO.class);
            ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
                 MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
                 MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                         mockParticipatedAuctionDAO(participatedAuctionDAO)) {
                assertThrows(AuctionNotStartedException.class, () -> auctionManager.placeBid(bid, null));

                verify(bidDAO, never()).insertBid(any(BidTransaction.class));
                verify(participatedAuctionDAO, never()).insert(any(BidTransaction.class));
            }
        }
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

}
