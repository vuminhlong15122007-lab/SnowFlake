package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.*;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.server.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO implements DAOInterface<User> {
    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);
    private static UserDAO instance;
    private UserDAO(){}
    public static UserDAO getInstance(){
        if (instance == null){
            instance = new UserDAO();
        }
        return instance;
    }


    @Override
    public int insert(User user) throws DataException {
        int result = 0;
        String sql = "INSERT INTO User (name, passWord, email, sdt, accountType, avt) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getName());
            pst.setString(2, PasswordHasher.hash(user.getPassWord()));
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getSdt());
            pst.setString(5, user.getAccountType() == null ? null : user.getAccountType().name());
            pst.setString(6, user.getImagePath());
            result = pst.executeUpdate();

            if (result > 0) {
                log.info("Tao user thanh cong");
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return result;
            }
            throw new DataInsertException("User");
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == 1062) {
                log.error("Lỗi: Dữ liệu bị trùng lặp!");
                if(e.getMessage().contains("user.unique_username")){
                    return -1;
                } else if(e.getMessage().contains("user.unique_sdt")){
                    return -2;
                } else if(e.getMessage().contains("user.unique_email")){
                    return -3;
                }
            }
            throw new DataInsertException("User");
        } catch (SQLException e) {
            log.error("Lỗi tạo user: {}", e.getMessage(), e);
            if (e.getSQLState().equals("23505")) {
                throw new DuplicateKeyException("User", "user name",user.getName());
            }
            throw new DataInsertException("User");
        }
    }

    @Override
    public int update(User user) throws DataException {
        int result = 0;
        String sql = "UPDATE User SET name = ?, passWord = ?, email = ?, sdt = ?, accountType = ?, avt = ? WHERE id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, user.getName());
            pst.setString(2, PasswordHasher.hash(user.getPassWord()));
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getSdt());
            pst.setString(5, user.getAccountType() == null ? null : user.getAccountType().name());
            pst.setString(6, user.getImagePath());
            pst.setInt(7, user.getId());
            result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("Cap nhat user thanh cong");
                return result;
            }
            throw new EntityNotFoundException("User", user.getId());
        } catch (SQLException e) {
            log.error("Lỗi cập nhật user: {}", e.getMessage(), e);
            throw new DataUpdateException(user.getId(), user.getName(), "info");        }

    }

    @Override
    public int delete(User user) throws DataException{
        int result = 0;
        String sql = "DELETE FROM User WHERE id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, user.getId());
            result = pst.executeUpdate();
            if (result > 0) {
                System.out.println("Xoa user thanh cong");
                return result;
            }
            throw new EntityNotFoundException("User", user.getId());

        } catch (SQLException e) {
            log.error("Lỗi xóa user: {}", e.getMessage(), e);
            throw new DataDeleteException(user.getId(), "User", "User");
        }
    }

    @Override
    public ArrayList<User> selectAll() throws DataException {
        ArrayList<User> result = new ArrayList<>();
        String sql = "SELECT id, name, email, passWord, sdt, accountType, avt FROM user";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql);
             ResultSet resultSet = pst.executeQuery()) {
            while (resultSet.next()) {
                result.add(mapUser(resultSet));
            }
        } catch (SQLException | IllegalArgumentException e) {
            log.error("Lỗi lấy danh sách user: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        }

        return result;
    }

    @Override
    public User selectById(int userId) throws DataException{
        String sql = "SELECT id, name, email, passWord, sdt, accountType, avt FROM user WHERE id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet resultSet = pst.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
            throw new EntityNotFoundException("User", userId);

        } catch (SQLException | IllegalArgumentException e) {
            log.error("Lỗi lấy user theo id: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        }

    }

    public User selectByUsername(String username) throws DataException{
        String sql = "SELECT id, name, email, passWord, sdt, accountType, avt FROM user WHERE name = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet resultSet = pst.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
            throw new EntityNotFoundException("User", username);

        } catch (SQLException | IllegalArgumentException e) {
            log.error("Lỗi lấy user theo username: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        }
    }

    public int getSize() throws DataException {
        String sql = "SELECT COUNT(*) FROM User";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Lỗi khi đếm user: {}", e.getMessage(), e);
            throw new QueryExecutionException(sql);
        }

        return 0;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String passWord = resultSet.getString("passWord");
        String sdt = resultSet.getString("sdt");
        String avatar = resultSet.getString("avt");
        String typeString = resultSet.getString("accountType");
        AccountType accountType = typeString == null || typeString.isBlank()
                ? null
                : AccountType.valueOf(typeString);

        return new User(id, name, passWord, email, sdt, accountType, avatar);
    }


}
