package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.*;
import com.javfxtutorial.hethongdaugia.common.model.domain.*;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionDAO implements DAOInterface<Auction> {
    private static final Logger log = LoggerFactory.getLogger(AuctionDAO.class);
    private static volatile AuctionDAO instance;
    private String BASE_QUERY =
            "\n"
                    + "SELECT \n"
                    + "    a.*, \n"
                    + "    i.name, \n"
                    + "    i.description, \n"
                    + "    i.imagepath, \n"
                    + "    i.idseller AS seller_id, \n"
                    + "    i.sellerName, \n"
                    + "    u.name AS winner_name,\n"
                    + "    u.email AS winner_email,\n"
                    + "    u.sdt AS winner_sdt,\n"
                    + "    i.category,\n"
                    + "    \n"
                    + "    -- Dữ liệu từ bảng 1 (brand, model - ví dụ: đồ điện tử/đồng hồ)\n"
                    + "    e.brand AS e_brand, \n"
                    + "    e.model,\n"
                    + "    \n"
                    + "    -- Dữ liệu từ bảng 2 (artist, year_created, title - ví dụ: các tác phẩm nghệ thuật)\n"
                    + "    art.artist, \n"
                    + "    art.year_created, \n"
                    + "    art.title,\n"
                    + "    \n"
                    + "    -- Dữ liệu từ bảng 3 (license_plate, year, brand, color - ví dụ: xe cộ)\n"
                    + "    v.license_plate, \n"
                    + "    v.year AS vehicle_year, \n"
                    + "    v.brand AS vehicle_brand, \n"
                    + "    v.color\n"
                    + "\n"
                    + "FROM auction a\n"
                    + "JOIN item i ON a.item_id = i.itemid\n"
                    + "\n"
                    + "-- Dùng LEFT JOIN để nếu item không thuộc loại này, nó vẫn ra kết quả (các cột kia sẽ null)\n"
                    + "LEFT JOIN electronics e ON i.itemid = e.item_id\n"
                    + "LEFT JOIN art art ON i.itemid = art.item_id\n"
                    + "LEFT JOIN vehicle v ON i.itemid = v.item_id\n"
                    + "LEFT JOIN user u ON a.winner_name = u.name";

    private AuctionDAO() {}

    public static AuctionDAO getInstance() {
        if (instance == null) {
            synchronized (AuctionDAO.class) {
                if (instance == null) {
                    instance = new AuctionDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(Auction auction) throws DataException {
        int result = 0;
        String sql =
                "INSERT INTO Auction(item_id, seller_id, init_price, step_price, current_price, winning_price, starting_time, ending_time, auctionStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, auction.getItem().getItemId());
            pst.setInt(2, auction.getSellerId());
            pst.setBigDecimal(3, auction.getInitPrice());
            pst.setBigDecimal(4, auction.getStepPrice());
            pst.setBigDecimal(5, auction.getCurrentPrice());
            pst.setBigDecimal(6, auction.getWinningPrice());
            pst.setTimestamp(7, Timestamp.valueOf(auction.getStartingTime()));
            pst.setTimestamp(8, Timestamp.valueOf(auction.getEndingTime()));
            pst.setString(9, String.valueOf(auction.getStatus()));
            result = pst.executeUpdate();
            log.info("Đang thực thi câu lệnh tạo Auction {}", sql);
            if (result > 0) {

                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        auction.setAuctionId(newId);
                    }
                }
                log.info("Tạo Auction thành công: {}", auction);
            } else {
                log.warn("Tạo Auction thất bại");
            }
        } catch (SQLException e) {
            log.error("Lỗi SQL khi tạo Auction: {}", e.getMessage(), e);
            throw new DataInsertException("Auction");
        }
        return result;
    }

    @Override
    public int update(Auction auction) throws DataException {
        int result = 0;
        String sql = "UPDATE Auction SET winner_name = ?,init_price = ?, step_price = ?, current_price = ?, winning_price = ?, starting_time = ?, ending_time = ?, auctionStatus =? WHERE auction_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, auction.getWinnerName());
            pst.setBigDecimal(2, auction.getInitPrice());
            pst.setBigDecimal(3, auction.getStepPrice());
            pst.setBigDecimal(4, auction.getCurrentPrice());
            pst.setBigDecimal(5, auction.getWinningPrice());
            pst.setTimestamp(6, Timestamp.valueOf(auction.getStartingTime()));
            pst.setTimestamp(7, Timestamp.valueOf(auction.getEndingTime()));
            pst.setString(8, String.valueOf(auction.getStatus()));
            pst.setInt(9, auction.getAuctionId());

            result = pst.executeUpdate();
            log.info("Bạn đang thực thi cập nhật Auction có ID: {}", auction.getAuctionId());

            if (result > 0) {
                log.info("Cập nhật Auction thành công!");
            } else {
                log.info(
                        "Cập nhật thất bại: Không tìm thấy Auction với ID = {}",
                        auction.getAuctionId());
                throw new EntityNotFoundException("Auction", auction.getAuctionId());
            }

        } catch (SQLException e) {
            log.error("Lỗi SQL khi cập nhật Auction: {}", e.getMessage(), e);
            throw new DataUpdateException(auction.getAuctionId(), "Auction", "update");
        }
        return result;
    }

    @Override
    public int delete(Auction auction) throws DataException {
        int result = 0;
        String sql = "DELETE FROM Auction WHERE auction_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, auction.getAuctionId());
            log.info("Bạn đang thực thi xóa Auction có ID: {}", auction.getAuctionId());
            result = pst.executeUpdate();

            if (result > 0) {
                log.info("Xóa Auction thành công");
            } else {
                log.info("Xóa thất bại");
                throw new EntityNotFoundException("Auction", auction.getAuctionId());
            }

        } catch (SQLException e) {
            log.error("Lỗi SQL khi xóa Auction: {}", e.getMessage(), e);
            throw new DataDeleteException(auction.getAuctionId(), "Auction", "delete");
        }
        return result;
    }

    public Auction mapResultSet(ResultSet rs) throws SQLException {
        // Map Item

        Item item = loadItemDetail(rs);

        // Map LocalDateTime
        LocalDateTime startingTime =
                rs.getTimestamp("starting_time") != null
                        ? rs.getTimestamp("starting_time").toLocalDateTime()
                        : null;
        LocalDateTime endingTime =
                rs.getTimestamp("ending_time") != null
                        ? rs.getTimestamp("ending_time").toLocalDateTime()
                        : null;

        // Map AuctionStatus
        AuctionStatus status = AuctionStatus.valueOf(rs.getString("auctionStatus"));

        Auction auction = new Auction(
                rs.getInt("auction_id"),
                item,
                rs.getInt("seller_id"),
                rs.getString("winner_name"),
                rs.getBigDecimal("init_price"),
                rs.getBigDecimal("current_price"),
                rs.getBigDecimal("step_price"),
                rs.getBigDecimal("winning_price"),
                startingTime,
                endingTime,
                status);
        return auction;
    }

    @Override
    public ArrayList<Auction> selectAll() throws DataException {
        ArrayList<Auction> list = new ArrayList<>();
        String sql = BASE_QUERY;

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            log.info("Đang lấy tất cả Auction từ database");
        } catch (SQLException e) {
            log.error("Lỗi SQL khi lấy danh sách Auction: {}", e.getMessage(), e);
            throw new QueryExecutionException(BASE_QUERY);
        }
        return list;
    }

    @Override
    public Auction selectById(int auctionId) throws DataException {
        String sql = BASE_QUERY + " WHERE a.auction_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);

            try (ResultSet rs = ps.executeQuery()) {
                log.info("Đang lấy Auction có ID: {}", auctionId);
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            log.error("Lỗi SQL khi lấy Auction: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        }
        return null;
    }

    public Auction selectByItemId(int id) throws DataException { // lấy auction dựa trên itemId
        Auction result = null;
        String sql = BASE_QUERY + " WHERE a.item_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, id);

            try (ResultSet resultSet = pst.executeQuery()) {
                log.info("Đang lấy Auction có item ID là: {}", id);
                if (resultSet.next()) {
                    result = mapResultSet(resultSet);
                }
            }

        } catch (SQLException e) {
            log.error("Lỗi SQL khi lấy Auction theo sellerId: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        } catch (NullPointerException e) {
            log.warn("Dữ liệu không tồn tại", e);
        }
        return result;
    }

    public ArrayList<Auction> selectBySellerId(int id)
            throws DataException { // lấy auction dựa trên sellerID
        ArrayList<Auction> list = new ArrayList<>();
        String sql = BASE_QUERY + " WHERE i.idseller = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                log.info("Đang lấy tất cả Auction tạo bởi sellerID: {}", id);
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Lỗi SQL khi lấy Auction theo sellerId: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        } catch (NullPointerException e) {
            log.warn("Dữ liệu không tồn tại", e);
        }
        return list;
    }

    public Item loadItemDetail(ResultSet rs) throws SQLException {
        ItemCategory category = ItemCategory.valueOf(rs.getString("category"));
        Item baseItem =
                new Item(
                        rs.getString("sellerName"),
                        rs.getInt("seller_id"),
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("imagepath"),
                        category);

        if (category == ItemCategory.ELECTRONICS) {
            return new Electronics(
                    baseItem.getSellerName(), baseItem.getSellerId(),
                    baseItem.getItemId(), baseItem.getName(),
                    baseItem.getDescription(), baseItem.getImage(),
                    rs.getString("e_brand"), rs.getString("model"));
        } else if (category == ItemCategory.ART) {
            return new Art(
                    baseItem.getSellerName(),
                    baseItem.getSellerId(),
                    baseItem.getItemId(),
                    baseItem.getName(),
                    baseItem.getDescription(),
                    baseItem.getImage(),
                    rs.getString("artist"),
                    rs.getInt("year_created"),
                    rs.getString("title"));
        } else if (category == ItemCategory.VEHICLE) {
            return new Vehicle(
                    baseItem.getSellerName(),
                    baseItem.getSellerId(),
                    baseItem.getItemId(),
                    baseItem.getName(),
                    baseItem.getDescription(),
                    baseItem.getImage(),
                    rs.getString("license_plate"),
                    rs.getInt("vehicle_year"),
                    rs.getString("vehicle_brand"),
                    rs.getString("color"));
        }
        return baseItem;
    }

    public ArrayList<Auction> selectUnpaidByWinnerId(int winnerId) throws DataException {
        ArrayList<Auction> list = new ArrayList<>();
        String sql = BASE_QUERY + " WHERE a.winner_id = ? AND a.auctionStatus = 'CLOSED'";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, winnerId);

            try (ResultSet rs = ps.executeQuery()) {
                log.info("Đang lấy Auction thắng bởi userID: {}", winnerId);
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Lỗi SQL khi lấy các Auction theo winnerID: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        } catch (NullPointerException e) {
            log.warn("Dữ liệu không tồn tại", e);
        }
        return list;
    }
}
