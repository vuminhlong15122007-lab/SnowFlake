package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAO  {
    private static BidDAO instance;
    private BidDAO(){}
    public static BidDAO getInstance(){
        if (instance == null){
            instance = new BidDAO();
        }
        return instance;
    }

    // 1. CREATE: Lưu một lượt đặt giá mới vào database
    public boolean insertBid(BidTransaction bid) {
        String sql = "INSERT INTO bid_transaction (bidder_id, auction_id, amount, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, bid.getBidderId());
            pstmt.setInt(2, bid.getAuctionId());
            pstmt.setDouble(3, bid.getAmount());
            // Convert LocalDate sang java.sql.Date
            pstmt.setDate(4, Date.valueOf(bid.getTimestamp()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. READ: Lấy toàn bộ lịch sử đặt giá của một phiên đấu giá (Sắp xếp từ cao xuống thấp)
    public List<BidTransaction> getBidsByAuctionId(int auctionId) {
        List<BidTransaction> bids = new ArrayList<>();
        String sql = "SELECT * FROM bid_transaction WHERE auction_id = ? ORDER BY amount DESC";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setBidId(rs.getInt("bid_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setAmount(rs.getDouble("amount"));
                    // Convert java.sql.Date về LocalDate
                    bid.setTimestamp(rs.getDate("timestamp").toLocalDate());

                    bids.add(bid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bids;
    }

    // 3. READ: Tìm lượt đặt giá cao nhất của một phiên đấu giá
    public BidTransaction getHighestBid(int auctionId) {
        String sql = "SELECT * FROM bid_transaction WHERE auction_id = ? ORDER BY amount DESC LIMIT 1";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setBidId(rs.getInt("bid_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setAmount(rs.getDouble("amount"));
                    bid.setTimestamp(rs.getDate("timestamp").toLocalDate());
                    return bid;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}