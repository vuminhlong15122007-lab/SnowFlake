package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtDAO extends BaseItemDAO<Art> {

    private ArtDAO() {}

    private static ArtDAO instance;

    public static ArtDAO getInstance() {
        if (instance == null) {
            synchronized (ArtDAO.class) {
                if (instance == null) {
                    instance = new ArtDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(Art art) {
        int result = insertBase(art);
        String sql = "INSERT INTO Art (item_id, artist, yearCreated, title) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, art.getItemId());
            pst.setString(2, art.getArtist());
            pst.setInt(3, art.getYearCreated());
            pst.setString(4, art.getTitle());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return result;
    }



    @Override
    public int update(Art art) {
        int result = updateBase(art);
        if (result == 0) return 0;
        String sql = "UPDATE Art SET artist = ?, yearCreated = ?, title = ? WHERE item_id = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, art.getArtist());
            pst.setInt(2, art.getYearCreated());
            pst.setString(3, art.getTitle());
            pst.setInt(4, art.getItemId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return result;
    }

    @Override
    public int delete(Art art) {
        return deleteBase(art);
    }

    @Override
    public List<Art> selectAll() {
        List<Art> list = new ArrayList<>();
        String sql = "SELECT i.*, a.artist, a.yearCreated, a.title " +
                "FROM Item i JOIN Art a ON i.itemid = a.item_id";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Art art = new Art(
                        rs.getString("sellerName"),
                        rs.getInt("idseller"),
                        rs.getInt("itemid"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getString("artist"),
                        rs.getInt("yearCreated"),
                        rs.getString("title")
                );
                list.add(art);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Art selectById(int id) {
        String sql = "SELECT i.*, a.artist, a.yearCreated, a.title " +
                "FROM Item i JOIN Art a ON i.itemid = a.item_id WHERE i.itemid = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Art(
                            rs.getString("sellerName"),
                            rs.getInt("idseller"),
                            rs.getInt("itemid"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("imagePath"),
                            rs.getString("artist"),
                            rs.getInt("yearCreated"),
                            rs.getString("title")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}