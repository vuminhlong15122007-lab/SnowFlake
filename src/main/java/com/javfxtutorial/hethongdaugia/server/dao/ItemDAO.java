package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.*;
import com.javfxtutorial.hethongdaugia.common.model.domain.Art;
import com.javfxtutorial.hethongdaugia.common.model.domain.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.domain.Vehicle;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import java.sql.*;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDAO implements DAOInterface<Item> {
  private static final Logger log = LoggerFactory.getLogger(ItemDAO.class);

  private ItemDAO() {}

  private static ItemDAO instance;

  public static ItemDAO getInstance() {
    if (instance == null) {
      synchronized (ItemDAO.class) {
        if (instance == null) {
          instance = new ItemDAO();
        }
      }
    }
    return instance;
  }

  public int insert(Item item) throws DataInsertException {
    String sql =
        "INSERT INTO Item (idseller, name, description, imagePath, sellerName, category) VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = JDBCUtil.getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        pst.setInt(1, item.getSellerId());
        pst.setString(2, item.getName());
        pst.setString(3, item.getDescription());
        pst.setString(4, item.getImage());
        pst.setString(5, item.getSellerName());
        pst.setString(6, item.getCategory().name());

        int result = pst.executeUpdate();
        if (result != 1) throw new SQLException("Insert Item failed");

        try (ResultSet rs = pst.getGeneratedKeys()) {
          if (!rs.next()) throw new SQLException("Cannot get generated itemId");
          item.setItemId(rs.getInt(1));
        }

        insertSubType(conn, item);

        conn.commit();
        return result;

      } catch (Exception e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          log.error("Rollback insert Item thất bại", rollbackEx);
        }
        throw new DataInsertException("Item");

      } finally {
        try {
          conn.setAutoCommit(true);
        } catch (SQLException autoCommitEx) {
          log.error("Không thể bật lại autoCommit", autoCommitEx);
        }
      }

    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi insert Item", e);
      throw new DataInsertException("Item");
    }
  }

  @Override
  public int update(Item item) throws DataUpdateException {
    int result = 0;

    // Câu lệnh SQL: Cập nhật thông tin dựa trên khóa chính là itemId
    // Giả định bạn không cho phép cập nhật idseller (vì người bán đã cố định), nếu cần bạn có
    // thể thêm vào SET
    String sql = "UPDATE Item SET name = ?, description = ?, imagePath = ? WHERE itemId = ?";

    // Sử dụng try-with-resources để tự động đóng Connection và PreparedStatement
    try (Connection connection = JDBCUtil.getConnection();
        PreparedStatement pst = connection.prepareStatement(sql)) {

      // Gán giá trị cho các dấu ? trong mệnh đề SET
      pst.setString(1, item.getName());
      pst.setString(2, item.getDescription());
      pst.setString(3, item.getImage());

      // Gán giá trị cho dấu ? trong mệnh đề WHERE (Quan trọng nhất)
      pst.setInt(4, item.getItemId());

      log.info("Bạn đang thực thi cập nhật Item có ID: {}", item.getItemId());

      // Thực thi câu lệnh
      result = pst.executeUpdate();

      // Kiểm tra kết quả
      if (result > 0) {
        updateSubType(connection, item);
        log.info("Cập nhật Item thành công!");
      } else {
        // Nếu result = 0 nghĩa là câu SQL chạy đúng, nhưng không tìm thấy ID nào khớp trong
        // Database
        log.info("Cập nhật thất bại: Không tìm thấy Item với ID = {}", item.getItemId());
      }

    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi update Item: {}", e.getMessage(), e);
      throw new DataUpdateException(item.getItemId(), "Item", "update");
    }

    return result;
  }

  public int delete(Item item) throws DataDeleteException {
    int result = 0;
    String deleteItemSQL = "DELETE FROM Item WHERE itemid = ?";
    try (Connection connection = JDBCUtil.getConnection();
        PreparedStatement pst = connection.prepareStatement(deleteItemSQL)) {
      pst.setInt(1, item.getItemId());
      result = pst.executeUpdate();
      log.info("Đã xóa {} Auction liên quan.", result);
    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi delete Item: {}", e.getMessage(), e);
      throw new DataDeleteException(item.getItemId(), "Item", "delete");
    }
    return result;
  }

  public ArrayList<Item> selectAll() throws DataException {
    ArrayList<Item> result = new ArrayList<>();
    String sql = null;
    try {
      Connection connection = JDBCUtil.getConnection();
      Statement st = connection.createStatement();
      // lenh sql
      sql = "SELECT * FROM item";
      log.info("Đang thực thi câu lệnh: {}", sql);
      ResultSet resultSet = st.executeQuery(sql);
      // lấy dữ liệu
      while (resultSet.next()) {
        String sellerName = resultSet.getString("sellerName");
        int idseller = resultSet.getInt("idseller");
        int iditem = resultSet.getInt("itemid");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        String imagePath = resultSet.getString("imagePath");
        String categoryStr = resultSet.getString("category");
        ItemCategory category = (categoryStr != null) ? ItemCategory.valueOf(categoryStr) : null;
        Item item = new Item(sellerName, idseller, iditem, name, description, imagePath, category);

        result.add(item);
      }
      JDBCUtil.closeConnection(connection);
    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi selectAll Item: {}", e.getMessage(), e);
      throw new QueryExecutionException(sql);
    }
    return result;
  }

  public Item selectById(int id) throws QueryExecutionException {
    Item result = null;
    String sql = null;
    try {
      Connection connection = JDBCUtil.getConnection(); // Tao ket noi
      Statement statement = connection.createStatement(); // tao ra obj statement
      // Thuc thi cau lech sql
      sql = "SELECT * FROM Item where  itemid = '" + id + "'";
      ResultSet resultSet = statement.executeQuery(sql);

      // tim kiem
      while (resultSet.next()) {
        int itemId = resultSet.getInt("itemid");
        int idseller = resultSet.getInt("idseller");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        String imagePath = resultSet.getString("imagePath");
        String sellerName = resultSet.getString("sellerName");
        String categoryStr = resultSet.getString("category");
        ItemCategory category = (categoryStr != null) ? ItemCategory.valueOf(categoryStr) : null;
        result = new Item(sellerName, idseller, itemId, name, description, imagePath, category);
      }
      // dong ket noi
      JDBCUtil.closeConnection(connection);
    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi selectById Item: {}", e.getMessage(), e);
      throw new QueryExecutionException(sql);
    }
    return result;
  }

  public void insertSubType(Connection conn, Item item) throws SQLException {
    if (item == null || item.getCategory() == null) {
      throw new SQLException("Item/category null");
    }

    switch (item.getCategory()) {
      case ART -> {
        if (!(item instanceof Art art)) throw new SQLException("ART nhưng object không phải Art");

        String sql = "INSERT INTO art (item_id, artist, year_created, title) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
          pst.setInt(1, art.getItemId());
          pst.setString(2, art.getArtist());
          pst.setInt(3, art.getYearCreated());
          pst.setString(4, art.getTitle());
          if (pst.executeUpdate() != 1) throw new SQLException("Insert ART subtype failed");
        }
      }

      case ELECTRONICS -> {
        if (!(item instanceof Electronics electronics))
          throw new SQLException("ELECTRONICS nhưng object không phải Electronics");

        String sql = "INSERT INTO electronics (item_id, brand, model) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
          pst.setInt(1, electronics.getItemId());
          pst.setString(2, electronics.getBrand());
          pst.setString(3, electronics.getModel());
          if (pst.executeUpdate() != 1) throw new SQLException("Insert ELECTRONICS subtype failed");
        }
      }

      case VEHICLE -> {
        if (!(item instanceof Vehicle vehicle))
          throw new SQLException("VEHICLE nhưng object không phải Vehicle");

        String sql =
            "INSERT INTO vehicle (item_id, license_plate, year, brand, color) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
          pst.setInt(1, vehicle.getItemId());
          pst.setString(2, vehicle.getLicensePlate());
          pst.setInt(3, vehicle.getYear());
          pst.setString(4, vehicle.getBrand());
          pst.setString(5, vehicle.getColor());
          if (pst.executeUpdate() != 1) throw new SQLException("Insert VEHICLE subtype failed");
        }
      }

      case OTHER -> {
        // Nếu OTHER không có bảng phụ thì cho qua.
      }
    }
  }

  public void updateSubType(Connection conn, Item item) throws SQLException {
    switch (item.getCategory()) {
      case ItemCategory.ART -> {
        Art art = (Art) item;
        String sql = "UPDATE art SET artist = ?, year_created = ?, title = ? WHERE item_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
          pst.setString(1, art.getArtist());
          pst.setInt(2, art.getYearCreated());
          pst.setString(3, art.getTitle());
          pst.setInt(4, art.getItemId());
          int result = pst.executeUpdate();
          if (result > 0) log.info("Cập nhật ART thành công, itemId: {}", art.getItemId());
          else log.warn("Cập nhật ART thất bại, itemId: {}", art.getItemId());
        }
      }
      case ItemCategory.ELECTRONICS -> {
        Electronics elec = (Electronics) item;
        String sql = "UPDATE electronics SET brand = ?, model = ? WHERE item_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
          pst.setString(1, elec.getBrand());
          pst.setString(2, elec.getModel());
          pst.setInt(3, elec.getItemId());
          int result = pst.executeUpdate();
          if (result > 0) log.info("Cập nhật ELECTRONICS thành công, itemId: {}", elec.getItemId());
          else log.warn("Cập nhật ELECTRONICS thất bại, itemId: {}", elec.getItemId());
        }
      }
      case ItemCategory.VEHICLE -> {
        Vehicle vehicle = (Vehicle) item;
        String sql =
            "UPDATE vehicle SET license_plate = ?, year = ?, brand = ?, color = ? WHERE item_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
          pst.setString(1, vehicle.getLicensePlate());
          pst.setInt(2, vehicle.getYear());
          pst.setString(3, vehicle.getBrand());
          pst.setString(4, vehicle.getColor());
          pst.setInt(5, vehicle.getItemId());
          int result = pst.executeUpdate();
          if (result > 0) log.info("Cập nhật VEHICLE thành công, itemId: {}", vehicle.getItemId());
          else log.warn("Cập nhật VEHICLE thất bại, itemId: {}", vehicle.getItemId());
        }
      }
      default -> log.warn("Category không xác định: {}", item.getCategory());
    }
  }
}
