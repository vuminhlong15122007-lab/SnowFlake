package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO extends BaseItemDAO<Vehicle> {
    private VehicleDAO() {}

    private static VehicleDAO instance;

    public static VehicleDAO getInstance() {
        if (instance == null) {
            synchronized (VehicleDAO.class) {
                if (instance == null) {
                    instance = new VehicleDAO();
                }
            }
        }
        return instance;
    }


    @Override
    public int insert(Vehicle vehicle) {
        int result = insertBase(vehicle);
        String sql = "insert into Vehicle (item_id, licensePlate, year, brand, color) values (?, ?, ?, ?, ?)";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, vehicle.getItemId());
            pst.setString(2, vehicle.getLicensePlate());
            pst.setInt(3, vehicle.getYear());
            pst.setString(4, vehicle.getBrand());
            pst.setString(5, vehicle.getColor());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return result;
    }



    @Override
    public int update(Vehicle vehicle) {
        int result = updateBase(vehicle);
        String sql = "update Vehicle set licensePlate = ?, year = ?, brand = ?, color = ? where item_id = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, vehicle.getLicensePlate());
            pst.setInt(2, vehicle.getYear());
            pst.setString(3, vehicle.getBrand());
            pst.setString(4, vehicle.getColor());
            pst.setInt(5, vehicle.getItemId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return result;

    }

    @Override
    public int delete(Vehicle vehicle) {
        return deleteBase(vehicle);
    }

    @Override
    public List<Vehicle> selectAll() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT i.*,a.licensePlate, a.year, a.brand, a.color  " +
                "FROM Item i JOIN Vehicle a ON i.itemid = a.item_id";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getString("sellerName"),
                        rs.getInt("idseller"),
                        rs.getInt("itemid"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getString("licensePlate"),
                        rs.getInt("year"),
                        rs.getString("brand"),
                        rs.getString("color")
                );
                list.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;

    }


    @Override
    public Vehicle selectById(int id) {
        String sql = "SELECT i.*,a.licensePlate, a.year, a.brand, a.color  " +
                "FROM Item i JOIN Vehicle a ON i.itemid = a.item_id WHERE i.itemid = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Vehicle(
                            rs.getString("sellerName"),
                            rs.getInt("idseller"),
                            rs.getInt("itemid"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("imagePath"),
                            rs.getString("licensePlate"),
                            rs.getInt("year"),
                            rs.getString("brand"),
                            rs.getString("color")
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
