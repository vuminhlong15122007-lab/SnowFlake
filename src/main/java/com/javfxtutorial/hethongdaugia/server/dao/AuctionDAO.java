package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AuctionDAO implements DAOInterface<Auction> {
    private static AuctionDAO instance;

    private AuctionDAO() {
    }

    ;

    public static AuctionDAO getInstance() {
        if (instance == null) {
            instance = new AuctionDAO();
        }
        return instance;
    }

    public int insert(Auction auction) {
        // 1. Viết câu SQL phù hợp với bảng Item
        Connection connection = JDBCUtil.getConnection();
        String sql = "INSERT INTO Auction(item_id, seller_id, init_price, step_price, starting_time, ending_time) VALUES (?,?, ?, ?, ?, ?)";
        int result = 0;

        // 2. Sử dụng try-with-resources để tự động quản lý kết nối
        try (
                PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 3. Truyền tham số an toàn vào câu SQL
            // Lưu ý chọn đúng kiểu dữ liệu: setString, setDouble, setInt...
            pst.setInt(1, auction.getItemId());
            pst.setInt(2, auction.getSellerId());
            pst.setDouble(3, auction.getInitPrice());
            pst.setDouble(4, auction.getStepPrice());
            pst.setTimestamp(5, Timestamp.valueOf(auction.getStartingTime()));
            pst.setTimestamp(6, Timestamp.valueOf(auction.getEndingTime()));


            System.out.println("Bạn đang thực thi thêm Auction" + sql);

            // 4. Thực thi
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Tạo Auction thành công");

                // 5. Lấy ID tự động sinh ra gán lại cho object
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
    public int update(Auction auction) { //chắc là để update trc khi phiên bắt đầu
        int result = 0;

        String sql = "UPDATE Auction SET init_price = ?, step_price = ?, starting_time = ?, ending_time = ? WHERE auction_id = ?";

        // Sử dụng try-with-resources để tự động đóng Connection và PreparedStatement
        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            // Gán giá trị cho các dấu ? trong mệnh đề SET
            pst.setDouble(1, auction.getInitPrice());
            pst.setDouble(2, auction.getStepPrice());
            pst.setTimestamp(3, Timestamp.valueOf(auction.getStartingTime()));
            pst.setTimestamp(4, Timestamp.valueOf(auction.getEndingTime()));

            // Gán giá trị cho dấu ? trong mệnh đề WHERE (Quan trọng nhất)
            pst.setInt(5, auction.getAuctionId());

            System.out.println("Bạn đang thực thi cập nhật Auction có ID: " + auction.getAuctionId());

            // Thực thi câu lệnh
            result = pst.executeUpdate();

            // Kiểm tra kết quả
            if (result > 0) {
                System.out.println("Cập nhật Auction thành công!");
            } else {
                // Nếu result = 0 nghĩa là câu SQL chạy đúng, nhưng không tìm thấy ID nào khớp trong Database
                System.out.println("Cập nhật thất bại: Không tìm thấy Auction với ID = " + auction.getAuctionId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi cập nhật Auctipn", e);
        }
        return result;
    }

    public int delete(Auction auction) {
        int result = 0;
        try {
            Connection connection = JDBCUtil.getConnection();

            // DÙNG PREPARED STATEMENT THAY VÌ STATEMENT (Tránh lỗi cú pháp và bảo mật)
            String sql = "DELETE FROM Auction WHERE auctionId = ?";
            PreparedStatement pst = connection.prepareStatement(sql);

            // Truyền ID vào vị trí dấu hỏi chấm
            pst.setInt(1, auction.getAuctionId());

            System.out.println(sql);
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Xóa Auction thành công");
            } else {
                System.out.println("Xóa thất bại");
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ObservableList<Auction> selectAll() {
        ObservableList<Auction> result = FXCollections.observableArrayList();
        try {
            Connection connection = JDBCUtil.getConnection();
            Statement st = connection.createStatement();
            String sql = "SELECT * FROM Auction";
            System.out.println(sql);
            ResultSet resultSet = st.executeQuery(sql);

            while (resultSet.next()) {
                int auctionId = resultSet.getInt("auctionId");
                int itemId = resultSet.getInt("itemId"); // Khóa ngoại trỏ tới bảng Item
                double initPrice = resultSet.getDouble("initPrice");
                double stepPrice = resultSet.getDouble("stepPrice");

                // Lấy thời gian từ DB và chuyển đổi sang LocalDateTime
                Timestamp startTimestamp = resultSet.getTimestamp("startTime");
                LocalDateTime startTime = (startTimestamp != null) ? startTimestamp.toLocalDateTime() : null;

                Timestamp endTimestamp = resultSet.getTimestamp("endTime");
                LocalDateTime endTime = (endTimestamp != null) ? endTimestamp.toLocalDateTime() : null;

                Auction auction = new Auction(auctionId, itemId, initPrice, stepPrice, startTime, endTime);
                result.add(auction);
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Auction selectById(int id) {
        Auction result = null;
        try {
            Connection connection = JDBCUtil.getConnection();

            // DÙNG PREPARED STATEMENT
            String sql = "SELECT * FROM Auction WHERE auctionId = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, id);

            ResultSet resultSet = pst.executeQuery();

            while (resultSet.next()) {
                int auctionId = resultSet.getInt("auctionId");
                int itemId = resultSet.getInt("itemId");
                double initPrice = resultSet.getDouble("initPrice");
                double stepPrice = resultSet.getDouble("stepPrice");

                Timestamp startTimestamp = resultSet.getTimestamp("startTime");
                LocalDateTime startTime = (startTimestamp != null) ? startTimestamp.toLocalDateTime() : null;

                Timestamp endTimestamp = resultSet.getTimestamp("endTime");
                LocalDateTime endTime = (endTimestamp != null) ? endTimestamp.toLocalDateTime() : null;

                String status = resultSet.getString("status");

                result = new Auction(auctionId, itemId, initPrice, stepPrice, startTime, endTime);
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<Auction> selectByCondition(String condition) {
        ArrayList<Auction> result = new ArrayList<>();
        try {
            Connection connection = JDBCUtil.getConnection();
            Statement statement = connection.createStatement();

            String sql = "SELECT * FROM Auction WHERE " + condition;
            System.out.println(sql);
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int auctionId = resultSet.getInt("auctionId");
                int itemId = resultSet.getInt("itemId");
                double initPrice = resultSet.getDouble("initPrice");
                double stepPrice = resultSet.getDouble("stepPrice");

                Timestamp startTimestamp = resultSet.getTimestamp("startTime");
                LocalDateTime startTime = (startTimestamp != null) ? startTimestamp.toLocalDateTime() : null;

                Timestamp endTimestamp = resultSet.getTimestamp("endTime");
                LocalDateTime endTime = (endTimestamp != null) ? endTimestamp.toLocalDateTime() : null;

                String status = resultSet.getString("status");

                result.add(new Auction(auctionId, itemId, initPrice, stepPrice, startTime, endTime));
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}



