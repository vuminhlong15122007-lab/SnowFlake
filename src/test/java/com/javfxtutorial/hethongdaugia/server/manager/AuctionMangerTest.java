package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuctionMangerTest {
    private AuctionManger auctionManger ;

    @BeforeEach
    void setup() throws SQLException {
        auctionManger = AuctionManger.getInstance() ;
    }

    @Test
    @DisplayName("đặt giá thành công nếu giá trị đầu vào hợp lệ")
    void testCheckValidBid_ShouldReturnTrue_WhenAmountIsCorrect(){
        Auction auction = new Auction();
        auction.setCurrentPrice(100.0);
        auction.setStepPrice(10.0);

        // giá bằng đúng giá hiện tại + step => hợp lệ
        assertTrue(auctionManger.checkValidBid(auction, 110.0));
        // giá mới lớn hơn giá hiện tại + step => hợp lệ
        assertTrue(auctionManger.checkValidBid(auction, 150.0));
    }

    @Test
    @DisplayName("đặt giá thất bại nếu giá trị nhỏ hơn giá trị đầu vào hợp lệ")
    void testCheckValidBid_ShouldReturnFalse_WhenAmountIsFalse(){
        Auction auction = new Auction();
        auction.setCurrentPrice(100.0);
        auction.setStepPrice(10.0);
        // giá nằm ở giữa giá hiện tại và giá hiện tại + step => false
        assertFalse(auctionManger.checkValidBid(auction , 105));
        // giá nằm ở dưới giá hiện tại
        assertFalse(auctionManger.checkValidBid(auction , 95));
    }
}
