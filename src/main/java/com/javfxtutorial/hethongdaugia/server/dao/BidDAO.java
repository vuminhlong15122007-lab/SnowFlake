package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DatabaseConnectionException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import java.sql.*;
import java.util.ArrayList;

public class BidDAO {
    private static volatile BidDAO instance;

    private BidDAO() {}

    public static BidDAO getInstance() {
        if (instance == null) {
            synchronized (BidDAO.class) {
                if (instance == null) {
                    instance = new BidDAO();
                }
            }
        }
        return instance;
    }

    // 1. CREATE: Lưu một lượt đặt giá mới vào database
    public boolean insertBid(BidTransaction bid) throws QueryExecutionException {
        String sql =
                "INSERT INTO bid_transaction (bidder_id, bidder_name, auction_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
                PreparedStatement pstmt =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, bid.getBidderId());
            pstmt.setString(2, bid.getBidderName());
            pstmt.setInt(3, bid.getAuctionId());
            pstmt.setBigDecimal(4, bid.getAmount());
            // Convert LocalDate sang java.sql.Date
            //            pstmt.setDate(4, Date.valueOf(bid.getTimestamp()));
            pstmt.setTimestamp(5, Timestamp.valueOf(bid.getTimestamp()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException | DatabaseConnectionException e) {
            throw new QueryExecutionException(sql);
        }
    }

    // 2. READ: Lấy toàn bộ lịch sử đặt giá của một phiên đấu giá (Sắp xếp từ cao xuống thấp)
    public ArrayList<BidTransaction> getBidsByAuctionId(int auctionId)
            throws QueryExecutionException {
        ArrayList<BidTransaction> bids = new ArrayList<>();
        String sql = "SELECT * FROM bid_transaction WHERE auction_id = ? ORDER BY amount DESC";

        try (Connection connection = JDBCUtil.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setBidId(rs.getInt("bid_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setBidderName(rs.getString("bidder_name"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setAmount(rs.getBigDecimal("amount"));
                    // Convert java.sql.Date về LocalDateTime
                    bid.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    bids.add(bid);
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new QueryExecutionException(sql);
        }
        return bids;
    }

    // 3. READ: Tìm lượt đặt giá cao nhất của một phiên đấu giá
    public BidTransaction getHighestBid(int auctionId) throws QueryExecutionException {
        String sql =
                "SELECT * FROM bid_transaction WHERE auction_id = ? ORDER BY amount DESC LIMIT 1";

        try (Connection connection = JDBCUtil.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setBidId(rs.getInt("bid_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setBidderName(rs.getString("bidder_name"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setAmount(rs.getBigDecimal("amount"));
                    bid.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    return bid;
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new QueryExecutionException(sql);
        }
        return null;
    }
}
