package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ParticipatedAuctionDAO {

  private ParticipatedAuctionDAO() {}
  private static volatile ParticipatedAuctionDAO instance;
  public static ParticipatedAuctionDAO getInstance() {
    if (instance == null) {
      synchronized (ParticipatedAuctionDAO.class) {
        if (instance == null) {
          instance = new ParticipatedAuctionDAO();
        }
      }
    }
    return instance;
  }

  public int insert(BidTransaction bid) {
    int result = 0;
    String sql = "INSERT INTO AuctionParticipation (bidderId, auctionId) VALUES (?, ?)";

    try (Connection con = JDBCUtil.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

      pst.setInt(1, bid.getBidderId());
      pst.setInt(2, bid.getAuctionId());

      result = pst.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return result;
  }


  public int delete(BidTransaction bid) {
    int result = 0;
    String sql = "DELETE FROM AuctionParticipation WHERE bidderId = ? AND auctionId = ?";

    try (Connection con = JDBCUtil.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

      pst.setInt(1, bid.getBidderId());
      pst.setInt(2, bid.getAuctionId());
      result = pst.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return result;
  }

  public List<Auction> getParticipatedAuctionsByBidder(int bidderId) {
    List<Auction> list = new ArrayList<>();
    // Câu query lấy thông tin đấu giá mà một người tham gia
    String sql =
        "SELECT \n" +
            "    a.*, \n" +
            "    pa.bidderId, \n" +               // <-- Thêm cột bidderId từ bảng Participation
            "    i.name, \n" +
            "    i.description, \n" +
            "    i.imagepath, \n" +
            "    i.idseller AS seller_id, \n" +
            "    i.sellerName, \n" +
            "    i.category,\n" +
            "    \n" +
            "    -- Dữ liệu từ bảng 1 (electronics)\n" +
            "    e.brand AS e_brand, \n" +
            "    e.model,\n" +
            "    \n" +
            "    -- Dữ liệu từ bảng 2 (art)\n" +
            "    art.artist, \n" +
            "    art.year_created, \n" +
            "    art.title,\n" +
            "    \n" +
            "    -- Dữ liệu từ bảng 3 (vehicle)\n" +
            "    v.license_plate, \n" +
            "    v.year AS vehicle_year, \n" +
            "    v.brand AS vehicle_brand, \n" +
            "    v.color\n" +
            "\n" +
            "FROM auction a\n" +
            "JOIN item i ON a.item_id = i.itemid\n" +
            "JOIN AuctionParticipation pa ON a.auction_id = pa.auctionId\n" + // <-- Thêm JOIN bảng Participation
            "\n" +
            "-- Dùng LEFT JOIN cho các bảng category con\n" +
            "LEFT JOIN electronics e ON i.itemid = e.item_id\n" +
            "LEFT JOIN art art ON i.itemid = art.item_id\n" +
            "LEFT JOIN vehicle v ON i.itemid = v.item_id\n" +
            "WHERE pa.bidderId = ?"; // <-- Thêm điều kiện lọc theo người đấu giá

    try (Connection con = JDBCUtil.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

      pst.setInt(1, bidderId);

      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          Auction auction = AuctionDAO.getInstance().mapResultSet(rs);
          list.add(auction);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }
}