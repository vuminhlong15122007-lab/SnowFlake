package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @Nested
    @DisplayName("Đặt giá")
    class PlaceBidTest{
    @Test
    @DisplayName("giá hợp lệ")
    void testCheckValidBid_ShouldReturnTrue_WhenAmountIsCorrect(){
        // giá bằng đúng giá hiện tại + step => hợp lệ
        assertTrue(auctionManager.checkValidBid(auction, new BigDecimal(110.0)));
        // giá mới lớn hơn giá hiện tại + step => hợp lệ
        assertTrue(auctionManager.checkValidBid(auction, new BigDecimal(150)));
    }

    @Test
    @DisplayName("giá nhỏ hơn giá trị hiện tại + step")
    void testCheckValidBid_ShouldReturnFalse_WhenAmountIsFalse(){
        // giá nằm ở giữa giá hiện tại và giá hiện tại + step => false
        assertFalse(auctionManager.checkValidBid(auction , new BigDecimal(105)));
        // giá nằm ở dưới giá hiện tại
        assertFalse(auctionManager.checkValidBid(auction , new BigDecimal(95)));
    }
    @Test
    @DisplayName("thất bại nếu là giá trị âm ")
    void invalid_negativeAmount() {

        assertFalse(auctionManager.checkValidBid(auction, new BigDecimal(-1.0)));
    }

    @Test
    @DisplayName("thất bại nếu giá là giá trị new")
    void invalid_zeroAmount() {
        assertFalse(auctionManager.checkValidBid(auction, new BigDecimal(0.0)));
    }
    @Test
    @DisplayName("Thành công giá quá lớn")
    void invalid_floatingPoint(){
        assertTrue(auctionManager.checkValidBid(auction, new BigDecimal(100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0)));
    }


    @Test
    @DisplayName("hợp lệ: giá số thực (floating point) chính xác")
    void valid_floatingPoint() {
        Auction auction = new Auction();
        auction.setCurrentPrice(new BigDecimal(99.5));
        auction.setStepPrice(new BigDecimal(0.5));
        assertTrue(auctionManager.checkValidBid(auction, new BigDecimal(100.0)));
        assertFalse(auctionManager.checkValidBid(auction, new BigDecimal(99.9)));
    }}
    @Nested
    @DisplayName("refreshAuctionStatus")
    class RefreshAuctionStatusTest {

        @Test
        @DisplayName("trả về NOT_START khi startingTime ở tương lai")
        void status_notStarted() {
            Auction auction = buildAuction(
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );
            AuctionStatus status = auctionManager.refreshAuctionStatus(auction);
            assertEquals(AuctionStatus.NOT_START, status);
            assertEquals(AuctionStatus.NOT_START, auction.getStatus());
        }

        @Test
        @DisplayName("trả về RUNNING khi đang trong thời gian đấu giá")
        void status_running() {
            Auction auction = buildAuction(
                    LocalDateTime.now().minusMinutes(30),
                    LocalDateTime.now().plusMinutes(30)
            );
            AuctionStatus status = auctionManager.refreshAuctionStatus(auction);
            assertEquals(AuctionStatus.RUNNING, status);
        }

        @Test
        @DisplayName("trả về CLOSED khi endingTime đã qua")
        void status_closed() {
            Auction auction = buildAuction(
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusMinutes(1)
            );
            AuctionStatus status = auctionManager.refreshAuctionStatus(auction);
            assertEquals(AuctionStatus.CLOSED, status);
        }

        @Test
        @DisplayName("trạng thái auction được cập nhật trực tiếp trên object")
        void status_updatesAuctionObject() {
            Auction auction = buildAuction(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1)
            );
            auction.setStatus(AuctionStatus.NOT_START); // sai ban đầu
            auctionManager.refreshAuctionStatus(auction);
            assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        }

        private Auction buildAuction(LocalDateTime start, LocalDateTime end) {
            Auction a = new Auction();
            a.setAuctionId(0); // id giả, không gọi DB
            a.setStartingTime(start);
            a.setEndingTime(end);
            a.setStatus(AuctionStatus.NOT_START);
            return a;
        }
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
    }
}
