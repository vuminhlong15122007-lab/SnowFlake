package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerTest {
    private AuctionManager auctionManager ;
    private Auction auction ;

    @BeforeEach
    void setup() {
        auctionManager = AuctionManager.getInstance() ;
        auction = new Auction();
        auction.setCurrentPrice(new BigDecimal(100.0));
        auction.setStepPrice(new BigDecimal(10));
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
        void stillRunning_within30Seconds() {
            Auction auction = new Auction();
            auction.setAuctionId(998);
            auction.setStartingTime(LocalDateTime.now().minusHours(1));
            auction.setEndingTime(LocalDateTime.now().plusSeconds(30));
            auction.setStatus(AuctionStatus.NOT_START);

            AuctionStatus status = auctionManager.refreshAuctionStatus(auction);
            assertEquals(AuctionStatus.RUNNING, status);
        }

        @Test
        @DisplayName("phiên còn 0 giây (đúng thời điểm kết thúc) là CLOSED")
        void closed_atEndingTime() {
            Auction auction = new Auction();
            auction.setAuctionId(997);
            auction.setStartingTime(LocalDateTime.now().minusHours(1));
            auction.setEndingTime(LocalDateTime.now().minusSeconds(1));
            auction.setStatus(AuctionStatus.RUNNING);

            AuctionStatus status = auctionManager.refreshAuctionStatus(auction);
            assertEquals(AuctionStatus.CLOSED, status);
        }
        @Test
        void sameMaxBid_shouldChooseEarlierUser_andPriceEqualsMaxBid() {
            BigDecimal step = new BigDecimal("10");
            BigDecimal minRequired = new BigDecimal("60");

            AutoBidConfig a = new AutoBidConfig(1, "A", 100, new BigDecimal("100"), true);
            AutoBidConfig b = new AutoBidConfig(2, "B", 100, new BigDecimal("100"), true);

            a.setRegisteredAt(LocalDateTime.of(2026, 1, 1, 10, 0));
            b.setRegisteredAt(LocalDateTime.of(2026, 1, 1, 10, 1));

            List<AutoBidConfig> bots = new ArrayList<>(List.of(a, b));

            bots.sort((b1, b2) -> {
                int cmp = b2.getMaxPrice().compareTo(b1.getMaxPrice());
                if (cmp != 0) return cmp;
                return b1.getRegisteredAt().compareTo(b2.getRegisteredAt());
            });

            AutoBidConfig winner = bots.get(0);
            AutoBidConfig second = bots.get(1);

            BigDecimal finalAmount;
            if (winner.getMaxPrice().compareTo(second.getMaxPrice()) == 0) {
                finalAmount = winner.getMaxPrice();
            } else {
                finalAmount = second.getMaxPrice().add(step);
                if (finalAmount.compareTo(winner.getMaxPrice()) > 0) {
                    finalAmount = winner.getMaxPrice();
                }
            }

            assertEquals(1, winner.getUserId());
            assertEquals(new BigDecimal("100"), finalAmount);
        }
    }

}
