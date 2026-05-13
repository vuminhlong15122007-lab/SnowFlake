package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuctionStatusTest {
    private AuctionManager auctionManager;

    @BeforeEach
    void setUp() {
        auctionManager = AuctionManager.getInstance();
    }
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

        // method nội bộ để build sẵn một Auction
        private Auction buildAuction(LocalDateTime start, LocalDateTime end) {
            Auction a = new Auction();
            a.setAuctionId(0); // id giả, không gọi DB
            a.setStartingTime(start);
            a.setEndingTime(end);
            a.setStatus(AuctionStatus.NOT_START);
            return a;
        }
    }
}
