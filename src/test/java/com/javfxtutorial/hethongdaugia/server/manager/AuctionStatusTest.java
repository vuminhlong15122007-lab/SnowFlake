package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        void status_notStarted() throws DataException {
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            Auction auction = buildAuction(
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
                AuctionStatus status = auctionManager.refreshAuctionStatus(auction);

                assertEquals(AuctionStatus.NOT_START, status);
                assertEquals(AuctionStatus.NOT_START, auction.getStatus());
                verify(auctionDAO, never()).update(any(Auction.class));
            }
        }

        @Test
        @DisplayName("trả về RUNNING khi đang trong thời gian đấu giá")
        void status_running() throws DataException {
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(any(Auction.class))).thenReturn(1);
            Auction auction = buildAuction(
                    LocalDateTime.now().minusMinutes(30),
                    LocalDateTime.now().plusMinutes(30)
            );

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
                AuctionStatus status = auctionManager.refreshAuctionStatus(auction);

                assertEquals(AuctionStatus.RUNNING, status);
                verify(auctionDAO).update(auction);
            } catch (DataException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("trả về CLOSED khi endingTime đã qua")
        void status_closed() throws DataException {
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(any(Auction.class))).thenReturn(1);
            Auction auction = buildAuction(
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusMinutes(1)
            );

            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
                AuctionStatus status = auctionManager.refreshAuctionStatus(auction);

                assertEquals(AuctionStatus.CLOSED, status);
                verify(auctionDAO).update(auction);
            }
        }

        @Test
        @DisplayName("trạng thái auction được cập nhật trực tiếp trên object")
        void status_updatesAuctionObject() throws DataException {
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(auctionDAO.update(any(Auction.class))).thenReturn(1);
            Auction auction = buildAuction(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1)
            );
            auction.setStatus(AuctionStatus.NOT_START); // sai ban đầu
            try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
                auctionManager.refreshAuctionStatus(auction);

                assertEquals(AuctionStatus.RUNNING, auction.getStatus());
                verify(auctionDAO).update(auction);
            } catch (DataException e) {
                throw new RuntimeException(e);
            }
        }

        // method nội bộ để build sẵn một Auction
        private Auction buildAuction(LocalDateTime start, LocalDateTime end) {
            Auction a = new Auction();
            a.setAuctionId(1);
            a.setStartingTime(start);
            a.setEndingTime(end);
            a.setStatus(AuctionStatus.NOT_START);
            return a;
        }

        private MockedStatic<AuctionDAO> mockAuctionDAO(AuctionDAO auctionDAO) {
            MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class);
            mockedAuctionDAO.when(AuctionDAO::getInstance).thenReturn(auctionDAO);
            return mockedAuctionDAO;
        }
    }
}
