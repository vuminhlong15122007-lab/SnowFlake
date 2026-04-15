package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.sql.*;
import java.util.ArrayList;

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
        // 1. Viết câu SQL phù hợp với bảng Item
        Connection connection = JDBCUtil.getConnection();
        String sql = "INSERT INTO Item (idseller, name, description, imagePath ) VALUES (?,?, ?, ?)";
        int result = 0;

        // 2. Sử dụng try-with-resources để tự động quản lý kết nối
        try (
             PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 3. Truyền tham số an toàn vào câu SQL
            // Lưu ý chọn đúng kiểu dữ liệu: setString, setDouble, setInt...
            pst.setInt(1, item.getSellerId());
            pst.setString(2, item.getName());
            pst.setString(3, item.getDescription());
            pst.setString(4, item.getImagePath());


            System.out.println("Bạn đang thực thi thêm Item: " + item.getName());

            // 4. Thực thi
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Tạo Item thành công");

                // 5. Lấy ID tự động sinh ra gán lại cho object
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        item.setItemId(newId);
                        System.out.println("ID tự động tạo cho Item là: " + item.getItemId());
                    }
                }
            } else {
                System.out.println("Tạo Item thất bại");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thao tác DB khi thêm Item", e);
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
            pst.setString(3, item.getImagePath());


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
//        int result = 0;
//        try {
//            //tao ket noi
//            Connection connection = JDBCUtil.getConnection();
//            //tao doi tuong statement
//            Statement st = connection.createStatement();
//            //thuc thi lenh sql
//            String sql = "DELETE FROM Item " + " WHERE ItemId='" + item.getItemId() + "'";
//            System.out.println(sql);
//            result = st.executeUpdate(sql);
//            if (result > 0){
//                System.out.println("Xóa Item thành công");
//            }
//            else{
//                System.out.println("Xóa thất bại");
//            }
//            JDBCUtil.closeConnection(connection);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return result;
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
                int idseller = resultSet.getInt("idseller");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String imagePath = resultSet.getString("imagePath");
                AccountType accountType = AccountType.valueOf(resultSet.getString("accountType"));
                Item item = new Item(idseller, name, description, imagePath);
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
                int idseller = resultSet.getInt("idseller");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String imagePath = resultSet.getString("imagePath");
                result = new Item(idseller, name, description, imagePath);
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
                int idseller = resultSet.getInt("idseller");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String imagePath = resultSet.getString("imagePath");
                result .add(new Item(idseller, name, description, imagePath));
            }
            //dong ket noi
            JDBCUtil.closeConnection(connection);
        }catch (SQLException e){
            e.printStackTrace(); // in ra loi xong van chay tiep
        }
        return result;

    }

    public ArrayList<Item> selectBySellerId(int sellerId) {         // dùng để ghép atribust ơr Item vs ở Auction
        ArrayList<Item> result = new ArrayList<>();
        String sql = "SELECT i.itemId, i.idseller, i.name, i.description, i.imagePath, " +
                "a.init_price, a.step_price " +
                "FROM Item i LEFT JOIN Auction a ON i.itemId = a.item_id " +
                "WHERE i.idseller = ? " +
                "ORDER BY a.starting_time DESC"; // Lấy phiên mới nhất nếu có nhiều
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, sellerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                item.setItemId(rs.getInt("itemId"));
                item.setSellerId(rs.getInt("idseller"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setImagePath(rs.getString("imagePath"));
                // Gán giá từ Auction (nếu không có Auction thì giá = 0)
                item.setCurrentPrice(rs.getDouble("init_price"));
                item.setStepPrice(rs.getDouble("step_price"));
                result.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
