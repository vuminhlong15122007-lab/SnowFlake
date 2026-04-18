package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO implements DAOInterface<Auction> {
    private static AuctionDAO instance;

    private AuctionDAO() {
    }

    public static AuctionDAO getInstance() {
        if (instance == null) {
            instance = new AuctionDAO();
        }
        return instance;
    }

    @Override
    public int insert(Auction auction) {
        int result = 0;
        String sql = "INSERT INTO Auction(item_id, seller_id, init_price, step_price, starting_time, ending_time, auctionStatus) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, auction.getItemId());
            pst.setInt(2, auction.getSellerId());
            pst.setDouble(3, auction.getInitPrice());
            pst.setDouble(4, auction.getStepPrice());
            pst.setTimestamp(5, Timestamp.valueOf(auction.getStartingTime()));
            pst.setTimestamp(6, Timestamp.valueOf(auction.getEndingTime()));
            pst.setString(7, String.valueOf(auction.getStatus()));

            System.out.println("Bạn đang thực thi thêm Auction: " + sql);
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Tạo Auction thành công");
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        auction.setAuctionId(newId);
                        System.out.println("ID tự động tạo cho Auction là: " + auction.getAuctionId());
                    }
                }
            } else {
                System.out.println("Tạo Auction thất bại");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi thêm Auction", e);
        }

        return result;
    }

    @Override
    public int update(Auction auction) {
        int result = 0;
        String sql = "UPDATE Auction SET winner_id = ?,init_price = ?, step_price = ?, current_price = ?, winning_price = ?, starting_time = ?, ending_time = ?, auctionStatus =? WHERE auction_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, auction.getWinnerId());
            pst.setDouble(2, auction.getInitPrice());
            pst.setDouble(3, auction.getStepPrice());
            pst.setDouble(4, auction.getCurrentPrice());
            pst.setDouble(5, auction.getWinningPrice());
            pst.setTimestamp(6, Timestamp.valueOf(auction.getStartingTime()));
            pst.setTimestamp(7, Timestamp.valueOf(auction.getEndingTime()));
            pst.setString(8, String.valueOf(auction.getStatus()));
            pst.setInt(9, auction.getAuctionId());




            System.out.println("Bạn đang thực thi cập nhật Auction có ID: " + auction.getAuctionId());
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Cập nhật Auction thành công!");
            } else {
                System.out.println("Cập nhật thất bại: Không tìm thấy Auction với ID = " + auction.getAuctionId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi cập nhật Auction", e);
        }
        return result;
    }

    @Override
    public int delete(Auction auction) {
        int result = 0;
        String sql = "DELETE FROM Auction WHERE auction_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, auction.getAuctionId());
            System.out.println("Bạn đang thực thi xóa Auction: " + sql);
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Xóa Auction thành công");
            } else {
                System.out.println("Xóa thất bại");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi xóa Auction", e);
        }
        return result;
    }

    @Override
    public List<Auction> selectAll() {
        List<Auction> result = new ArrayList<>();
        String sql = "SELECT * FROM Auction";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql);
             ResultSet resultSet = pst.executeQuery()) {

            while (resultSet.next()) {
                result.add(extractAuctionFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi lấy danh sách Auction", e);
        }
        return result;
    }

    @Override
    public Auction selectById(int id) {  // lấy auction bằng auction id
        Auction result = null;
        String sql = "SELECT * FROM Auction WHERE auction_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, id);

            try (ResultSet resultSet = pst.executeQuery()) {
                if (resultSet.next()) {
                    result = extractAuctionFromResultSet(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo ID", e);
        }
        return result;
    }

    public Auction selectByItemId(int id) {      // lấy auction dựa trên itemId
        Auction result = null;
        String sql = "SELECT * FROM Auction WHERE item_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, id);

            try (ResultSet resultSet = pst.executeQuery()) {
                if (resultSet.next()) {
                    result = extractAuctionFromResultSet(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo Item ID", e);
        } catch (NullPointerException e){
            System.out.println("dữ liệu k tồn tại");
        }
        return result;
    }

    public ArrayList<Auction> selectByCondition(String condition) {
        ArrayList<Auction> result = new ArrayList<>();
        // Lưu ý: Nối chuỗi condition trực tiếp vào câu lệnh có thể gây ra rủi ro SQL Injection
        // Khuyến nghị: Thiết kế lại hàm này truyền vào tên cột và giá trị thay vì cả đoạn chuỗi điều kiện
        String sql = "SELECT * FROM Auction WHERE " + condition;

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql);
             ResultSet resultSet = pst.executeQuery()) {

            System.out.println("Đang lấy Auction với điều kiện: " + sql);
            while (resultSet.next()) {
                result.add(extractAuctionFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo Condition", e);
        }
        return result;
    }

    // --- Hàm Hỗ Trợ Dùng Chung (Giúp code gọn gàng, không bị lặp lại) ---
    private Auction extractAuctionFromResultSet(ResultSet resultSet) throws SQLException {
        int auctionId = resultSet.getInt("auction_id");
        int itemId = resultSet.getInt("item_id");
        int sellerId = resultSet.getInt("seller_id");
        int winnerId = resultSet.getInt("winner_id");
        double initPrice = resultSet.getDouble("init_price");
        double currentPrice = resultSet.getDouble("current_price");
        double stepPrice = resultSet.getDouble("step_price");
        double winningPrice = resultSet.getDouble("winning_price");
        AuctionStatus auctionStatus = AuctionStatus.valueOf(resultSet.getString("AuctionStatus"));

        Timestamp startTimestamp = resultSet.getTimestamp("starting_time");
        LocalDateTime startTime = (startTimestamp != null) ? startTimestamp.toLocalDateTime() : null;

        Timestamp endTimestamp = resultSet.getTimestamp("ending_time");
        LocalDateTime endTime = (endTimestamp != null) ? endTimestamp.toLocalDateTime() : null;

        return new Auction(auctionId, itemId, sellerId, winnerId, initPrice, currentPrice, stepPrice, winningPrice, startTime, endTime, auctionStatus);
    }
}