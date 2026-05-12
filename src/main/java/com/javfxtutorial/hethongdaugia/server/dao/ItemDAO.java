package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.*;
import java.util.ArrayList;

public class ItemDAO implements DAOInterface<Item> {
  private static final Logger log = LoggerFactory.getLogger(ItemDAO.class);

  private ItemDAO() {
  }

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

  public int insert(Item item) {
    String sql = "INSERT INTO Item (idseller, name, description, imagePath, sellerName, category) VALUES (?, ?, ?, ?, ?, ?)";
    int result = 0;
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pst.setInt(1, item.getSellerId());
      pst.setString(2, item.getName());
      pst.setString(3, item.getDescription());
      pst.setString(4, item.getImage());
      pst.setString(5, item.getSellerName());
      pst.setString(6, item.getCategory().name());

      result = pst.executeUpdate();
      if (result > 0) {
        try (ResultSet rs = pst.getGeneratedKeys()) {
          if (rs.next()) {
            item.setItemId(rs.getInt(1));
          }
        }
        log.info("Tạo Item thành công, ID: {}", item.getItemId());

        insertSubType(conn, item);
      } else {
        log.info("Tạo Item thất bại");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public int update(Item item) {
    int result = 0;

    // Câu lệnh SQL: Cập nhật thông tin dựa trên khóa chính là itemId
    // Giả định bạn không cho phép cập nhật idseller (vì người bán đã cố định), nếu cần bạn có thể thêm vào SET
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

      System.out.println("Bạn đang thực thi cập nhật Item có ID: " + item.getItemId());

      // Thực thi câu lệnh
      result = pst.executeUpdate();

      // Kiểm tra kết quả
      if (result > 0) {
        updateSubType(connection, item);
        log.info("Cập nhật Item thành công!");
      } else {
        // Nếu result = 0 nghĩa là câu SQL chạy đúng, nhưng không tìm thấy ID nào khớp trong Database
        log.info("Cập nhật thất bại: Không tìm thấy Item với ID = {}", item.getItemId());
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Lỗi thao tác DB khi cập nhật Item", e);
    }

    return result;
  }

  public int delete(Item item) {
    int result = 0;
    String deleteItemSQL = "DELETE FROM Item WHERE item_id = ?";
    try (Connection connection = JDBCUtil.getConnection();
         PreparedStatement pst = connection.prepareStatement(deleteItemSQL)) {
      pst.setInt(1, item.getItemId());
      result = pst.executeUpdate();
      log.info("Đã xóa {} Auction liên quan.", result);
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return result;
  }


  public ArrayList<Item> selectAll() {
    ArrayList<Item> result = new ArrayList<>();
    try {
      Connection connection = JDBCUtil.getConnection();
      Statement st = connection.createStatement();
      //lenh sql
      String sql = "SELECT * FROM item";
      System.out.println(sql);
      ResultSet resultSet = st.executeQuery(sql);
      //lấy dữ liệu
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
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return result;
  }
  public Item selectById(int id) {
    Item result = null;
    try {
      Connection connection = JDBCUtil.getConnection(); // Tao ket noi
      Statement statement = connection.createStatement(); // tao ra obj statement
      // Thuc thi cau lech sql
      String sql = "SELECT * FROM Item where  itemid = '" + id + "'";
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
      //dong ket noi
      JDBCUtil.closeConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace(); // in ra loi xong van chay tiep
    }
    return result;
  }

  ;

  public ArrayList<Item> selectByCondition(String condition) {
    ArrayList<Item> result = new ArrayList<>();
    try {
      Connection connection = JDBCUtil.getConnection(); // Tao ket noi
      Statement statement = connection.createStatement(); // tao ra obj statement
      // Thuc thi cau lech sql
      String sql = "SELECT * FROM item where " + condition;
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
        Item item = new Item(sellerName, idseller, itemId, name, description, imagePath, category);
        result.add(item);
      }
      //dong ket noi
      JDBCUtil.closeConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace(); // in ra loi xong van chay tiep
    }
    return result;

  }

  public void insertSubType(Connection conn, Item item) throws SQLException {
    switch (item.getCategory()) {
      case ItemCategory.ART -> {
        Art item1 = (Art) item;
        String sql1 = "INSERT INTO art (item_id, artist, year_created, title) VALUES (?, ?, ?, ?)";
        int result1 = 0;
        try (PreparedStatement pst = conn.prepareStatement(sql1)) {
          pst.setInt(1, item1.getItemId());
          pst.setString(2, item1.getArtist());
          pst.setInt(3, item1.getYearCreated());
          pst.setString(4, item1.getTitle());
          result1 = pst.executeUpdate();
          if (result1 > 0) {
            log.info("Thêm bảng phụ ART cho thành công cho itemId: {}", item.getItemId());
          } else {
            log.info("Thêm bảng phụ ART cho thất bại cho itemId: {}", item.getItemId());
            delete(item);
          }
        }
      }
      case ItemCategory.ELECTRONICS -> {
        Electronics item2 = (Electronics) item;
        String sql2 = "INSERT INTO electronic (item_id, brand, model) VALUES (?, ?, ?)";
        int result2 = 0;
        try (PreparedStatement pst = conn.prepareStatement(sql2)) {
          pst.setInt(1, item2.getItemId());
          pst.setString(2, item2.getBrand());
          pst.setString(3, item2.getModel());
          result2 = pst.executeUpdate();
          if (result2 > 0) {
            log.info("Thêm bảng phụ ELECTRONICS cho thành công cho itemId: {}", item.getItemId());
          } else {
            log.info("Thêm bảng phụ ELECTRONICS cho thất bại cho itemId: {}", item.getItemId());
            delete(item);
          }
        }
      }
      case ItemCategory.VEHICLE -> {
        Vehicle item3 = (Vehicle) item;
        String sql3 = "INSERT INTO vehicle (item_id, license_plate, year, brand, color) VALUES (?, ?, ?, ?, ?)";
        int result3 = 0;
        try (PreparedStatement pst = conn.prepareStatement(sql3)) {
          pst.setInt(1, item3.getItemId());
          pst.setString(2, item3.getLicensePlate());
          pst.setInt(3, item3.getYear());
          pst.setString(4, item3.getBrand());
          pst.setString(5, item3.getColor());
          result3 = pst.executeUpdate();
          if (result3 > 0) {
            log.info("Thêm bảng phụ VEHICLE cho thành công cho itemId: {}", item.getItemId());
          } else {
            log.info("Thêm bảng phụ VEHICLE cho thất bại cho itemId: {}", item.getItemId());
            delete(item);
          }
        }
      }
      default -> log.warn("Category không xác định: {}", item.getCategory());
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
        String sql = "UPDATE electronic SET brand = ?, model = ? WHERE item_id = ?";
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
        String sql = "UPDATE vehicle SET license_plate = ?, year = ?, brand = ?, color = ? WHERE item_id = ?";
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
