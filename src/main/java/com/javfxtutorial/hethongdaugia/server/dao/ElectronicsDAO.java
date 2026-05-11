package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;

import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ElectronicsDAO extends BaseItemDAO<Electronics> {
    private ElectronicsDAO(){};
    private static ElectronicsDAO instance;
    public static ElectronicsDAO getInstance(){
        if(instance == null){
            synchronized (ElectronicsDAO.class){
                if(instance == null){
                    instance = new ElectronicsDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(Electronics electronics) {
        int result = insertBase(electronics);
        String sql = "insert into Electronics (item_id, brand, model) values (?, ?, ?)";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, electronics.getItemId());
            pst.setString(2, electronics.getBrand());
            pst.setString(3, electronics.getModel());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return result;
    }


    @Override
    public int update(Electronics electronics) {
        int result = updateBase(electronics);
        String sql = "update Electronics set brand = ?, model = ? where item_id = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, electronics.getBrand());
            pst.setString(2, electronics.getModel());
            pst.setInt(3, electronics.getItemId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return result;
    }

    @Override
    public int delete(Electronics electronics) {
        return deleteBase(electronics);
    }

    @Override
    public List<Electronics> selectAll() {
        List<Electronics> list = new ArrayList<>();
        String sql = "SELECT i.*, a.brand, a.model " +
                "FROM Item i JOIN Electronics a ON i.itemid = a.item_id";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Electronics electronics = new Electronics(
                        rs.getString("sellerName"),
                        rs.getInt("idseller"),
                        rs.getInt("itemid"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getString("brand"),
                        rs.getString("model")
                );
                list.add(electronics);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Electronics selectById(int id) {
        String sql = "SELECT i.*,a.brand, a.model  " +
                "FROM Item i JOIN Electronics a ON i.itemid = a.item_id WHERE i.itemid = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Electronics(
                            rs.getString("sellerName"),
                            rs.getInt("idseller"),
                            rs.getInt("itemid"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("imagePath"),
                            rs.getString("brand"),
                            rs.getString("model")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
