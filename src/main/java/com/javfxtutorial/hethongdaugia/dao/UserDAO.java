package com.javfxtutorial.hethongdaugia.dao;

import com.javfxtutorial.hethongdaugia.database.JBDCUtil;
import com.javfxtutorial.hethongdaugia.model.User;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO implements DAOInterface<User> {
    public static UserDAO getInstance() {
        return new UserDAO();
    }

    @Override
    public int create(User user) {
        Connection connection = JBDCUtil.getConnection();
        int result = 0;
        try {
            Statement st = connection.createStatement();
            String sql = "insert into User (id, name, passWord, email)\n" +
                    "values (\"" + user.getId() + "\" , \"" + user.getName() + "\" , \"" + user.getPassWord() + "\" , \"" + user.getEmail() + "\");";

            System.out.println("Bạn đang thực thi: " + sql);
            result = st.executeUpdate(sql);

            if (result > 0) {
                System.out.println("Tạo user thành công");
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
        return 0;
    }

    @Override
    public int delete(User user) {
        return 0;
    }

    @Override
    public ArrayList<User> selectAll() {
        return null;
    }

    @Override
    public User selectById(User user) {
        return null;
    }

    @Override
    public ArrayList<User> selectByCondition(String condition) {
        return null;
    }

    public int getSize() {
        Connection connection = JBDCUtil.getConnection();
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
