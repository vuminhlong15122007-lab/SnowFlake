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
        "SELECT a.*, pa.bidderId, i.name, i.description, i.imagepath, i.sellerName, i.category " +
            "FROM auction a " +
            "JOIN item i ON a.item_id = i.itemid " +
            "JOIN AuctionParticipation pa ON a.auction_id = pa.auctionId " +
            "WHERE pa.bidderId = ?";

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