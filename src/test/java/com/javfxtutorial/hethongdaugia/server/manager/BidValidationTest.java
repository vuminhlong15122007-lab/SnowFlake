package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BidValidationTest {
    private AuctionManager auctionManager ;
    private Auction auction ;
    @BeforeEach
    void setup() {
        auctionManager = AuctionManager.getInstance() ;
        auction = new Auction();
        auction.setCurrentPrice(new BigDecimal("100.0"));
        auction.setStepPrice(new BigDecimal("10"));
    }
    @Nested
    @DisplayName("Đặt giá")
    class PlaceBidTest{
        @Test
        @DisplayName("giá hợp lệ")
        void testCheckValidBid_ShouldReturnTrue_WhenAmountIsCorrect(){
            // giá bằng đúng giá hiện tại + step => hợp lệ
            assertTrue(auctionManager.checkValidBid(auction, new BigDecimal("110.0")));
            // giá mới lớn hơn giá hiện tại + step => hợp lệ
            assertTrue(auctionManager.checkValidBid(auction, new BigDecimal("150")));
        }

        @Test
        @DisplayName("giá nhỏ hơn giá trị hiện tại + step")
        void testCheckValidBid_ShouldReturnFalse_WhenAmountIsFalse(){
            // giá nằm ở giữa giá hiện tại và giá hiện tại + step => false
            assertFalse(auctionManager.checkValidBid(auction , new BigDecimal("105")));
            // giá nằm ở dưới giá hiện tại
            assertFalse(auctionManager.checkValidBid(auction , new BigDecimal("95")));
        }
        @Test
        @DisplayName("thất bại nếu là giá trị âm ")
        void invalid_negativeAmount() {

            assertFalse(auctionManager.checkValidBid(auction, new BigDecimal("-1.0")));
        }

        @Test
        @DisplayName("thất bại nếu giá là giá trị new")
        void invalid_zeroAmount() {
            assertFalse(auctionManager.checkValidBid(auction, new BigDecimal("0.0")));
        }
        @Test
        @DisplayName("Thành công giá quá lớn")
        void invalid_floatingPoint(){
            assertTrue(auctionManager.checkValidBid(auction, new BigDecimal("100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0")));
        }


        @Test
        @DisplayName("hợp lệ: giá số thực (floating point) chính xác")
        void valid_floatingPoint() {
            Auction auction = new Auction();
            auction.setCurrentPrice(new BigDecimal("99.5"));
            auction.setStepPrice(new BigDecimal("0.5"));
            assertTrue(auctionManager.checkValidBid(auction, new BigDecimal("100.0")));
            assertFalse(auctionManager.checkValidBid(auction, new BigDecimal("99.9")));
        }
    }
}
