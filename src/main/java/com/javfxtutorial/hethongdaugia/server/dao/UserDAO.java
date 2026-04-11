package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO implements DAOInterface<User> {
    public static UserDAO getInstance() {
        return new UserDAO();
    }

    @Override
    public int insert(User user) {
        Connection connection = JDBCUtil.getConnection();
        int result = 0;
        try {
            Statement st = connection.createStatement();

            // 1. BỎ cột 'id' ra khỏi câu sql để MySQL tự động quyết định ID
            String sql = "insert into User (name, passWord, email, sdt, accountType)\n" +
                    "values (\"" + user.getName()
                    + "\" , \"" + user.getPassWord()
                    + "\" , \"" + user.getEmail()
                    + "\" , \"" + user.getSdt()
                    + "\" , \"" + user.getAccountType()
                    + "\");";

            System.out.println("Bạn đang thực thi: " + sql);

            // 2. Thêm cờ Statement.RETURN_GENERATED_KEYS vào hàm executeUpdate
            result = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            if (result > 0) {
                System.out.println("Tạo user thành công");

                // 3. Lấy ID do MySQL vừa tạo ra và gán lại cho đối tượng user
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1); // Lấy giá trị ở cột đầu tiên của ResultSet
                    user.setId(newId);        // Gán ngược lại cho user
                    System.out.println("ID tự động tạo là: " + user.getId());
                }
                rs.close(); // Nhớ đóng ResultSet
            } else {
                System.out.println("Tạo user thất bại");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    @Override
    public int update(User user) {
        Connection connection = JDBCUtil.getConnection();
        int result = 0;

        // Câu lệnh SQL với các dấu ? đại diện cho giá trị sẽ truyền vào sau
        // QUAN TRỌNG: Bắt buộc phải có WHERE id = ?, nếu không nó sẽ update TOÀN BỘ bảng!
        String sql = "UPDATE User " +
                "SET name = \"" + user.getName()
                + "\" , password = \"" + user.getPassWord()
                + "\" , email = \"" + user.getEmail()
                + "\" , sdt = \"" + user.getSdt()
                + "\" , accountType = \"" + user.getAccountType()
                + "\" WHERE id = " + user.getId() +
                ";";

        try {
            System.out.println("Bạn đang thực thi" + sql);
            Statement st = connection.createStatement();
            // Chạy câu lệnh
            result = st.executeUpdate(sql);

            if (result > 0) {
                System.out.println("Cập nhật user thành công!");
            } else {
                System.out.println("Cập nhật thất bại (có thể ID không tồn tại trong database).");
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public int delete(User user) {
        int result = 0;
        try {
            //tao ket noi
            Connection connection = JDBCUtil.getConnection();
            //tao doi tuong statement
            Statement st = connection.createStatement();
            //thuc thi lenh sql
            String sql = "DELETE FROM User " + " WHERE id='" + user.getId() + "'";
            System.out.println(sql);
            result = st.executeUpdate(sql);
            if (result > 0){
                System.out.println("Xóa user thành công");
            }
            else{
                System.out.println("Xóa thất bại");
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ArrayList<User> selectAll() {
        ArrayList<User> result = new ArrayList<>();
        try {
            Connection connection = JDBCUtil.getConnection();
            Statement st = connection.createStatement();
            //lenh sql
            String sql = "SELECT * FROM user";
            System.out.println(sql);
            ResultSet resultSet = st.executeQuery(sql);
            //lấy dữ liệu
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String passWord = resultSet.getString("passWord");
                String sdt = resultSet.getString("sdt");
                AccountType accountType = AccountType.valueOf(resultSet.getString("accountType"));
                User user = new User(id, name, passWord, email, sdt, accountType);
                result.add(user);
            }
            JDBCUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public User selectById(int userId) {
        User result = null;
        try{
            Connection connection = JDBCUtil.getConnection(); // Tao ket noi
            Statement statement = connection.createStatement(); // tao ra obj statement
            // Thuc thi cau lech sql
            String sql = "SELECT * FROM user where  id = '" + userId + "'";
            ResultSet resultSet = statement.executeQuery(sql);

            // tim kiem
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String passWord = resultSet.getString("passWord");
                String sdt = resultSet.getString("sdt");
                AccountType accountType = AccountType.valueOf(resultSet.getString("Accounttype"));
                result = new User(id, name, passWord, email, sdt, accountType);
            }
            //dong ket noi
            JDBCUtil.closeConnection(connection);
        }catch (SQLException e){
            e.printStackTrace(); // in ra loi xong van chay tiep
        }
        return result;
    }

    public ArrayList<User> selectByCondition(String condition) {
        ArrayList<User> result =  new ArrayList<>();
        try{
            Connection connection = JDBCUtil.getConnection(); // Tao ket noi
            Statement statement = connection.createStatement(); // tao ra obj statement
            // Thuc thi cau lech sql
            String sql = "SELECT * FROM user where " + condition ;
            ResultSet resultSet = statement.executeQuery(sql);

            // tim kiem
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String passWord = resultSet.getString("passWord");
                String sdt = resultSet.getString("sdt");
                AccountType accountType = AccountType.valueOf(resultSet.getString("Accounttype"));
                User user = new User(id, name, passWord, email, sdt, accountType);
                result.add(user);
            }
            //dong ket noi
            JDBCUtil.closeConnection(connection);
        }catch (SQLException e){
            e.printStackTrace(); // in ra loi xong van chay tiep
        }
        return result;

    }

    public User selectByUsername(String username) {
        User result = null;
        try{
            Connection connection = JDBCUtil.getConnection(); // Tao ket noi
            Statement statement = connection.createStatement(); // tao ra obj statement
            // Thuc thi cau lech sql
            String sql = "SELECT * FROM user where name = \"" + username + '"';
            ResultSet resultSet = statement.executeQuery(sql);

            // tim kiem
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String passWord = resultSet.getString("passWord");
                String sdt = resultSet.getString("sdt");
                AccountType accountType = AccountType.valueOf(resultSet.getString("Accounttype"));
                result = new User(id, name, passWord, email, sdt, accountType);
            }
            //dong ket noi
            JDBCUtil.closeConnection(connection);
        }catch (SQLException e){
            e.printStackTrace(); // in ra loi xong van chay tiep
        }
        return result;

    }

    public int getSize() {
        Connection connection = JDBCUtil.getConnection();
        int count = 0;
        ResultSet result;
        try {
            String sql = "SELECT COUNT(*) FROM User";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            System.out.println("Bạn đang thực thi: " + sql);
            ResultSet rs = pstmt.executeQuery(sql);
            if (rs.next()) {
                // Lấy giá trị của cột đầu tiên (chính là kết quả của COUNT(*))
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
}
