package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Item;

import java.sql.*;

public abstract class BaseItemDAO<T extends Item> implements DAOInterface<T> {

    protected int insertBase(Item item) {
        String sql = "INSERT INTO Item (idseller, name, description, imagePath, sellerName, category) VALUES (?,?,?,?,?,?)";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, item.getSellerId());
            pst.setString(2, item.getName());
            pst.setString(3, item.getDescription());
            pst.setString(4, item.getImage());
            pst.setString(5, item.getSellerName());
            pst.setString(6, item.getCategory().name());
            int result = pst.executeUpdate();
            if (result > 0) {
                ResultSet keys = pst.getGeneratedKeys();
                if (keys.next()) item.setItemId(keys.getInt(1));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected int updateBase(Item item) {
        String sql = "UPDATE Item SET name=?, description=?, imagePath=? WHERE itemid=?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, item.getName());
            pst.setString(2, item.getDescription());
            pst.setString(3, item.getImage());
            pst.setInt(4, item.getItemId());
            return pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected int deleteBase(Item item) {
        String sql = "DELETE FROM Item WHERE itemid=?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, item.getItemId());
            return pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}