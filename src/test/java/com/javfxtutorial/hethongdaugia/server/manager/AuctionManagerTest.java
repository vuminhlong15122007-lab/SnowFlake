package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionAlreadyEndedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ParticipatedAuctionDAO;
import com.javfxtutorial.hethongdaugia.server.network.BidListener;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AuctionManager.placeBid")
public class AuctionManagerTest {
    private AuctionManager auctionManager;

    @BeforeEach
    void setup() throws Exception {
        auctionManager = AuctionManager.getInstance();
        TestStateSupport.resetAuctionManager(auctionManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestStateSupport.resetAuctionManager(auctionManager);
    }

    @Test
    @DisplayName("đặt giá hợp lệ lưu DB, thông báo realtime và gia hạn anti-snipe")
    void placeBid_acceptsValidBid_persistsBid_notifiesSubscribersAndExtendsNearEndAuction() throws Exception {
        Auction auction = runningAuction(100, "100", "10");
        LocalDateTime originalEnd = LocalDateTime.now().plusSeconds(30);
        auction.setEndingTime(originalEnd);
        TestStateSupport.activeAuctions(auctionManager).put(auction.getAuctionId(), auction);

        RecordingBidListener listener = new RecordingBidListener();
        auctionManager.registerToAuction(listener, auction.getAuctionId());

        BidTransaction bid = bid(auction.getAuctionId(), 20, "alice", "150");
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
            auctionManager.placeBid(bid, null);

            assertEquals(new BigDecimal("150"), auction.getCurrentPrice());
            assertEquals(20, auction.getWinnerId());
            assertEquals("alice", auction.getWinnerName());
            assertEquals(originalEnd.plusSeconds(60), auction.getEndingTime());
            assertEquals(originalEnd.plusSeconds(60), bid.getNewEndingTime());

            assertEquals(1, listener.callCount);
            assertSame(bid, listener.lastBid);
            assertEquals(bid, bidCaptor.getValue());
            verify(auctionDAO).update(auction);
            verify(participatedAuctionDAO).insert(bid);
        }
    }

    @Test
    @DisplayName("không cho đặt giá khi phiên chưa bắt đầu và không ghi DB")
    void placeBid_rejectsAuctionBeforeStartAndDoesNotPersistBid() throws Exception {
        Auction auction = runningAuction(101, "100", "10");
        auction.setStartingTime(LocalDateTime.now().plusMinutes(5));
        auction.setEndingTime(LocalDateTime.now().plusHours(1));
        auction.setStatus(AuctionStatus.NOT_START);
        TestStateSupport.activeAuctions(auctionManager).put(auction.getAuctionId(), auction);

        BidTransaction bid = bid(auction.getAuctionId(), 20, "alice", "200");
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

    @Test
    @DisplayName("load auction từ DB khi cache chưa có và đặt giá thành công")
    void placeBid_loadsAuctionFromDaoWhenCacheMiss() throws Exception {
        Auction auction = runningAuction(104, "100", "10");
        BidTransaction bid = bid(auction.getAuctionId(), 20, "alice", "150");
        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        BidDAO bidDAO = mock(BidDAO.class);
        ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);
        when(auctionDAO.selectById(auction.getAuctionId())).thenReturn(auction);
        when(auctionDAO.update(auction)).thenReturn(1);
        when(bidDAO.insertBid(bid)).thenReturn(true);
        when(participatedAuctionDAO.insert(bid)).thenReturn(1);

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
             MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
             MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                     mockParticipatedAuctionDAO(participatedAuctionDAO)) {
            assertTrue(auctionManager.placeBid(bid, null));

            assertSame(auction, TestStateSupport.activeAuctions(auctionManager).get(auction.getAuctionId()));
            assertEquals(new BigDecimal("150"), auction.getCurrentPrice());
            verify(auctionDAO).selectById(auction.getAuctionId());
            verify(auctionDAO).update(auction);
            verify(bidDAO).insertBid(bid);
        }
    }

    @Test
    @DisplayName("báo lỗi khi cache và DB đều không có auction")
    void placeBid_throwsAuctionNotFoundWhenDaoCannotFindAuction() throws Exception {
        BidTransaction bid = bid(404, 20, "alice", "150");
        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        when(auctionDAO.selectById(404)).thenReturn(null);

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO)) {
            assertThrows(AuctionNotFoundException.class, () -> auctionManager.placeBid(bid, null));

            verify(auctionDAO).selectById(404);
        }
    }

    @Test
    @DisplayName("không cho đặt giá khi phiên đã kết thúc")
    void placeBid_rejectsEndedAuctionAndDoesNotPersistBid() throws Exception {
        Auction auction = runningAuction(105, "100", "10");
        auction.setEndingTime(LocalDateTime.now().minusMinutes(5));
        auction.setStatus(AuctionStatus.CLOSED);
        TestStateSupport.activeAuctions(auctionManager).put(auction.getAuctionId(), auction);

        BidTransaction bid = bid(auction.getAuctionId(), 20, "alice", "150");
        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        BidDAO bidDAO = mock(BidDAO.class);
        ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
             MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
             MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                     mockParticipatedAuctionDAO(participatedAuctionDAO)) {
            assertThrows(AuctionAlreadyEndedException.class, () -> auctionManager.placeBid(bid, null));

            verify(bidDAO, never()).insertBid(any(BidTransaction.class));
            verify(participatedAuctionDAO, never()).insert(any(BidTransaction.class));
        }
    }

    @Test
    @DisplayName("vẫn đặt giá thành công khi bản ghi tham gia đã tồn tại")
    void placeBid_ignoresDuplicateParticipatedAuctionRecord() throws Exception {
        Auction auction = runningAuction(106, "100", "10");
        TestStateSupport.activeAuctions(auctionManager).put(auction.getAuctionId(), auction);

        BidTransaction bid = bid(auction.getAuctionId(), 20, "alice", "150");
        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        BidDAO bidDAO = mock(BidDAO.class);
        ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);
        when(auctionDAO.update(auction)).thenReturn(1);
        when(bidDAO.insertBid(bid)).thenReturn(true);
        when(participatedAuctionDAO.insert(bid)).thenThrow(
                new DuplicateKeyException("ParticipatedAuction", "auctionId/userId", auction.getAuctionId())
        );

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
             MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
             MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                     mockParticipatedAuctionDAO(participatedAuctionDAO)) {
            assertTrue(auctionManager.placeBid(bid, null));

            assertEquals(new BigDecimal("150"), auction.getCurrentPrice());
            verify(auctionDAO).update(auction);
            verify(bidDAO).insertBid(bid);
            verify(participatedAuctionDAO).insert(bid);
        }
    }

    @Test
    @DisplayName("không cho người bán tự đặt giá sản phẩm của mình")
    void placeBid_rejectsBidFromSellerAndDoesNotPersistBid() throws Exception {
        Auction auction = runningAuction(102, "100", "10");
        TestStateSupport.activeAuctions(auctionManager).put(auction.getAuctionId(), auction);

        BidTransaction bid = bid(auction.getAuctionId(), auction.getSellerId(), "seller", "150");
        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        BidDAO bidDAO = mock(BidDAO.class);
        ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
             MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
             MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                     mockParticipatedAuctionDAO(participatedAuctionDAO)) {
            assertThrows(SelfBidException.class, () -> auctionManager.placeBid(bid, null));

            verify(bidDAO, never()).insertBid(any(BidTransaction.class));
            verify(participatedAuctionDAO, never()).insert(any(BidTransaction.class));
        }
    }

    @Test
    @DisplayName("không lưu bid khi giá chưa đủ bước nhảy tối thiểu")
    void placeBid_rejectsBidBelowStepAndDoesNotPersistBid() throws Exception {
        Auction auction = runningAuction(103, "100", "10");
        TestStateSupport.activeAuctions(auctionManager).put(auction.getAuctionId(), auction);

        BidTransaction bid = bid(auction.getAuctionId(), 20, "alice", "105");
        AuctionDAO auctionDAO = mock(AuctionDAO.class);
        BidDAO bidDAO = mock(BidDAO.class);
        ParticipatedAuctionDAO participatedAuctionDAO = mock(ParticipatedAuctionDAO.class);

        try (MockedStatic<AuctionDAO> mockedAuctionDAO = mockAuctionDAO(auctionDAO);
             MockedStatic<BidDAO> mockedBidDAO = mockBidDAO(bidDAO);
             MockedStatic<ParticipatedAuctionDAO> mockedParticipatedDAO =
                     mockParticipatedAuctionDAO(participatedAuctionDAO)) {
            assertThrows(InsufficientIncrementException.class, () -> auctionManager.placeBid(bid, null));

            verify(bidDAO, never()).insertBid(any(BidTransaction.class));
            verify(participatedAuctionDAO, never()).insert(any(BidTransaction.class));
        }
    }

    private static Auction runningAuction(int auctionId, String currentPrice, String stepPrice) {
        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSellerId(10);
        auction.setCurrentPrice(new BigDecimal(currentPrice));
        auction.setStepPrice(new BigDecimal(stepPrice));
        auction.setWinningPrice(new BigDecimal(currentPrice));
        auction.setStartingTime(LocalDateTime.now().minusMinutes(5));
        auction.setEndingTime(LocalDateTime.now().plusMinutes(5));
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }

    private static BidTransaction bid(int auctionId, int bidderId, String bidderName, String amount) {
        BidTransaction bid = new BidTransaction();
        bid.setAuctionId(auctionId);
        bid.setBidderId(bidderId);
        bid.setBidderName(bidderName);
        bid.setAmount(new BigDecimal(amount));
        bid.setTimestamp(LocalDateTime.now());
        return bid;
    }

    private static MockedStatic<AuctionDAO> mockAuctionDAO(AuctionDAO auctionDAO) {
        MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class);
        mockedAuctionDAO.when(AuctionDAO::getInstance).thenReturn(auctionDAO);
        return mockedAuctionDAO;
    }

    private static MockedStatic<BidDAO> mockBidDAO(BidDAO bidDAO) {
        MockedStatic<BidDAO> mockedBidDAO = mockStatic(BidDAO.class);
        mockedBidDAO.when(BidDAO::getInstance).thenReturn(bidDAO);
        return mockedBidDAO;
    }

    private static MockedStatic<ParticipatedAuctionDAO> mockParticipatedAuctionDAO(
            ParticipatedAuctionDAO participatedAuctionDAO
    ) {
        MockedStatic<ParticipatedAuctionDAO> mockedParticipatedAuctionDAO =
                mockStatic(ParticipatedAuctionDAO.class);
        mockedParticipatedAuctionDAO.when(ParticipatedAuctionDAO::getInstance)
                .thenReturn(participatedAuctionDAO);
        return mockedParticipatedAuctionDAO;
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
