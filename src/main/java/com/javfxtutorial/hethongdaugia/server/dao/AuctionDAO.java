package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.*;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory.ELECTRONICS;

public class AuctionDAO implements DAOInterface<Auction> {
  private static final Logger log = LoggerFactory.getLogger(AuctionDAO.class);
  private static volatile AuctionDAO instance;
  private String BASE_QUERY =
          "SELECT a.*, i.name, i.description, i.imagepath, " +
                  "i.idseller AS seller_id, i.sellerName, i.category " +
                  "FROM auction a " +
                  "JOIN item i ON a.item_id = i.itemid ";


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
  public int insert(Auction auction) {
    int result = 0;
    String sql = "INSERT INTO Auction(item_id, seller_id, init_price, step_price, current_price, winning_price, starting_time, ending_time, auctionStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection connection = JDBCUtil.getConnection();
         PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        System.out.println("Tạo Auction thất bại");
      }
    }  catch (SQLException e) {
      e.printStackTrace();
      return 0; // ← thay thế

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
        log.info("Cập nhật thất bại: Không tìm thấy Auction với ID = {}", auction.getAuctionId());
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
      log.info("Bạn đang thực thi xóa Auction có ID: {}", auction.getAuctionId());
      result = pst.executeUpdate();

      if (result > 0) {
        log.info("Xóa Auction thành công");
      } else {
        log.info("Xóa thất bại");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Lỗi thao tác DB khi xóa Auction", e);
    }
    return result;
  }

  public Auction mapResultSet(ResultSet rs) throws SQLException {
    // Map Item
    String cat = rs.getString("category");
    ItemCategory category;
    if (cat != null){
      category = ItemCategory.valueOf(cat.toUpperCase());
    }else category = null;
    Item baseItem = new Item(
            rs.getString("sellerName"),
            rs.getInt("seller_id"),
            rs.getInt("item_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("imagepath"),
            category
    );

    int itemId = rs.getInt("item_id");
    Item item = loadItemDetail(itemId, category, baseItem);

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
        rs.getBigDecimal("init_price"),
        rs.getBigDecimal("current_price"),
        rs.getBigDecimal("step_price"),
        rs.getBigDecimal("winning_price"),
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
      log.info("Đang lấy tất cả Auction từ database");
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
        log.info("Đang lấy Auction có ID: {}", auctionId);
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
        log.info("Đang lấy Auction có item ID là: {}", id);
        if (resultSet.next()) {
          result = mapResultSet(resultSet);
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

  public ArrayList<Auction> selectBySellerId(int id) {      // lấy auction dựa trên sellerID
    ArrayList<Auction> list = new ArrayList<>();
    String sql = BASE_QUERY + "WHERE i.idseller = ?";

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
      e.printStackTrace();
      throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo Seller ID", e);
    } catch (NullPointerException e) {
      System.out.println("dữ liệu k tồn tại");
    }
    return list;
  }


  public Item loadItemDetail(int itemId, ItemCategory category, Item baseItem) {
    if (category == null) return baseItem;

    if (category == ItemCategory.ELECTRONICS) {
      String sql = "SELECT brand, model FROM electronic WHERE item_id = ?";
      try (Connection conn = JDBCUtil.getConnection();
           PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setInt(1, itemId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
          return new Electronics(
                  baseItem.getSellerName(), baseItem.getSellerId(),
                  baseItem.getItemId(), baseItem.getName(),
                  baseItem.getDescription(), baseItem.getImage(),
                  rs.getString("brand"), rs.getString("model")
          );
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

    } else if (category == ItemCategory.ART) {
      String sql = "SELECT artist, year_created, title FROM art WHERE item_id = ?";
      try (Connection conn = JDBCUtil.getConnection();
           PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setInt(1, itemId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
          return new Art(
                  baseItem.getSellerName(), baseItem.getSellerId(),
                  baseItem.getItemId(), baseItem.getName(),
                  baseItem.getDescription(), baseItem.getImage(),
                  rs.getString("artist"),
                  rs.getInt("year_created"),
                  rs.getString("title")
          );
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

    } else if (category == ItemCategory.VEHICLE) {
      String sql = "SELECT license_plate, year, brand, color FROM vehicle WHERE item_id = ?";
      try (Connection conn = JDBCUtil.getConnection();
           PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setInt(1, itemId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
          return new Vehicle(
                  baseItem.getSellerName(), baseItem.getSellerId(),
                  baseItem.getItemId(), baseItem.getName(),
                  baseItem.getDescription(), baseItem.getImage(),
                  rs.getString("license_plate"),
                  rs.getInt("year"),
                  rs.getString("brand"),
                  rs.getString("color")
          );
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return baseItem;
  }

  public ArrayList<Auction> selectByWinnerId(int winnerId) {      // lấy auction dựa trên sellerID
    ArrayList<Auction> list = new ArrayList<>();
    String sql = BASE_QUERY + "WHERE a.winner_id = ?";

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
      e.printStackTrace();
      throw new RuntimeException("Lỗi thao tác DB khi lấy Auction theo Winner ID", e);
    } catch (NullPointerException e) {
      System.out.println("dữ liệu k tồn tại");
    }
    return list;
  }
}

