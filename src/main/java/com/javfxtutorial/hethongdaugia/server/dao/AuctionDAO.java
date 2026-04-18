package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO implements DAOInterface<Auction> {
    private static AuctionDAO instance;
    private String BASE_QUERY =
            "SELECT a.*, i.name, i.description, i.imagepath, i.idseller AS seller_id_item, i.sellerName " +
                    "FROM auction a " +
                    "JOIN item i ON a.item_id = i.itemid ";


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

            pst.setInt(1, auction.getItem().getItemId());
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

    private Auction mapResultSet(ResultSet rs) throws SQLException {
        // Map Item
        Item item = new Item(
                rs.getInt("item_id"),
                rs.getInt("seller_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("imagepath"),
                rs.getString("sellerName")
        );

        // Map LocalDateTime
        LocalDateTime startingTime = rs.getTimestamp("starting_time") != null
                ? rs.getTimestamp("starting_time").toLocalDateTime() : null;
        LocalDateTime endingTime = rs.getTimestamp("ending_time") != null
                ? rs.getTimestamp("ending_time").toLocalDateTime() : null;

        // Map AuctionStatus
        AuctionStatus status = AuctionStatus.valueOf(rs.getString("auctionStatus"));

        return new Auction(
                rs.getInt("auction_id"),
                item,
                rs.getInt("seller_id"),
                rs.getInt("winner_id"),
                rs.getDouble("init_price"),
                rs.getDouble("current_price"),
                rs.getDouble("step_price"),
                rs.getDouble("winning_price"),
                startingTime,
                endingTime,
                status
        );
    }

    @Override
    public ArrayList<Auction> selectAll() {
        ArrayList<Auction> list = new ArrayList<>();
        String sql = BASE_QUERY;

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Auction selectById(int auctionId) {
        String sql = BASE_QUERY + "WHERE a.auction_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Auction selectByItemId(int id) {      // lấy auction dựa trên itemId
        Auction result = null;
        String sql = "SELECT * FROM Auction WHERE item_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, id);

            try (ResultSet resultSet = pst.executeQuery()) {
                if (resultSet.next()) {
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo Item ID", e);
        } catch (NullPointerException e) {
            System.out.println("dữ liệu k tồn tại");
        }
        return result;
    }

    public ArrayList<Auction> selectBySellerId(int id) {      // lấy auction dựa trên itemId
        ArrayList<Auction> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE i.idseller = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo Seller ID", e);
        } catch (NullPointerException e) {
            System.out.println("dữ liệu k tồn tại");
        }
        return list;
    }
}

