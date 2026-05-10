package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO implements DAOInterface<Item> {
    private ItemDAO(){};
    private static ItemDAO instance;
    public static ItemDAO getInstance(){
        if (instance == null){
            synchronized (ItemDAO.class){
                if (instance == null){
                    instance = new ItemDAO();
                }
            }
        }
        return  instance;
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
                System.out.println("Tạo Item thành công, ID: " + item.getItemId());
            } else {
                System.out.println("Tạo Item thất bại");
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
                System.out.println("Cập nhật Item thành công!");
            } else {
                // Nếu result = 0 nghĩa là câu SQL chạy đúng, nhưng không tìm thấy ID nào khớp trong Database
                System.out.println("Cập nhật thất bại: Không tìm thấy Item với ID = " + item.getItemId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi cập nhật Item", e);
        }

        return result;
    }
    public int delete(Item item){
        Connection connection = JDBCUtil.getConnection();
        try {
            connection.setAutoCommit(false); // Bắt đầu transaction

            // 1. Xóa tất cả Auction liên quan đến Item này
            String deleteAuctionSQL = "DELETE FROM Auction WHERE item_id = ?";
            try (PreparedStatement pstAuction = connection.prepareStatement(deleteAuctionSQL)) {
                pstAuction.setInt(1, item.getItemId());
                int auctionRows = pstAuction.executeUpdate();
                System.out.println("Đã xóa " + auctionRows + " Auction liên quan.");
            }

            // 2. Xóa Item
            String deleteItemSQL = "DELETE FROM Item WHERE ItemId = ?";
            try (PreparedStatement pstItem = connection.prepareStatement(deleteItemSQL)) {
                pstItem.setInt(1, item.getItemId());
                int result = pstItem.executeUpdate();

                connection.commit(); // Thành công → lưu thay đổi
                System.out.println("Xóa Item thành công, ID = " + item.getItemId());
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback(); // Có lỗi → quay lui
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {}
            JDBCUtil.closeConnection(connection);
        }
    }

    public ArrayList<Item> selectAll(){
        ArrayList<Item> result = new ArrayList<>();
        try {
            Connection connection = JDBCUtil.getConnection();
            Statement st = connection.createStatement();
            //lenh sql
            String sql = "SELECT * FROM item";
            System.out.println(sql);
            ResultSet resultSet = st.executeQuery(sql);
            //lấy dữ liệu
            while (resultSet.next()){
                String sellerName = resultSet.getString("sellerName");
                int idseller = resultSet.getInt("idseller");
                int iditem = resultSet.getInt("itemid");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String imagePath = resultSet.getString("imagePath");
                String categoryStr = resultSet.getString("category");
                ItemCategory category = (categoryStr != null) ? ItemCategory.valueOf(categoryStr) : null;
                Item item = new Item(sellerName, idseller,iditem, name, description, imagePath, category);


                result.add(item);
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    };
    public Item selectById(int id){
        Item result = null;
        try{
            Connection connection = JDBCUtil.getConnection(); // Tao ket noi
            Statement statement = connection.createStatement(); // tao ra obj statement
            // Thuc thi cau lech sql
            String sql = "SELECT * FROM Item where  itemid = '" + id + "'";
            ResultSet resultSet = statement.executeQuery(sql);

            // tim kiem
            while (resultSet.next()){
                int itemId = resultSet.getInt("itemid");
                int idseller = resultSet.getInt("idseller");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String imagePath = resultSet.getString("imagePath");
                String sellerName = resultSet.getString("sellerName");
                String categoryStr = resultSet.getString("category");
                ItemCategory category = (categoryStr != null) ? ItemCategory.valueOf(categoryStr) : null;
                result = new Item(sellerName, idseller,itemId, name, description, imagePath, category);

            }
            //dong ket noi
            JDBCUtil.closeConnection(connection);
        }catch (SQLException e){
            e.printStackTrace(); // in ra loi xong van chay tiep
        }
        return result;
    };

    public ArrayList<Item> selectByCondition(String condition){
        ArrayList<Item> result =  new ArrayList<>();
        try{
            Connection connection = JDBCUtil.getConnection(); // Tao ket noi
            Statement statement = connection.createStatement(); // tao ra obj statement
            // Thuc thi cau lech sql
            String sql = "SELECT * FROM item where " + condition ;
            ResultSet resultSet = statement.executeQuery(sql);

            // tim kiem
            while (resultSet.next()){
                int itemId = resultSet.getInt("itemid");
                int idseller = resultSet.getInt("idseller");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String imagePath = resultSet.getString("imagePath");
                String sellerName = resultSet.getString("sellerName");
                String categoryStr = resultSet.getString("category");
                ItemCategory category = (categoryStr != null) ? ItemCategory.valueOf(categoryStr) : null;
                Item item = new Item(sellerName, idseller,itemId, name, description, imagePath, category);
                result.add(item);
            }
            //dong ket noi
            JDBCUtil.closeConnection(connection);
        }catch (SQLException e){
            e.printStackTrace(); // in ra loi xong van chay tiep
        }
        return result;

    }

}
