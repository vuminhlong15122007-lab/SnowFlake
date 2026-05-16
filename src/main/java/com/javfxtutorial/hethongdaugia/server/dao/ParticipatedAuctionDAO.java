package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DatabaseConnectionException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipatedAuctionDAO {

  private static final Logger log = LoggerFactory.getLogger(ParticipatedAuctionDAO.class);

  private ParticipatedAuctionDAO() {
  }

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

  public int insert(BidTransaction bid) throws DuplicateKeyException, QueryExecutionException {
    int result = 0;
    String sql = "INSERT INTO AuctionParticipation (bidderId, auctionId) VALUES (?, ?)";

    try (Connection con = JDBCUtil.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

      pst.setInt(1, bid.getBidderId());
      pst.setInt(2, bid.getAuctionId());

      result = pst.executeUpdate();
    } catch (SQLIntegrityConstraintViolationException e) {
    // Lỗi duplicate key - người dùng đã tham gia rồi
    log.info("Người dùng {} đã tham gia phiên đấu giá {} rồi",
            bid.getBidderId(), bid.getAuctionId());
    throw new DuplicateKeyException("AuctionParticipation",
            "bidderId=" + bid.getBidderId() + ", auctionId=" + bid.getAuctionId(), bid.getBidderName());
  } catch (SQLException | DatabaseConnectionException e) {
    log.error("Lỗi SQL khi insert AuctionParticipation: {}", e.getMessage(), e);
    throw new QueryExecutionException(sql);
  }
    return result;
  }


  public int delete(BidTransaction bid) throws QueryExecutionException {
    int result = 0;
    String sql = "DELETE FROM AuctionParticipation WHERE bidderId = ? AND auctionId = ?";

    try (Connection con = JDBCUtil.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

      pst.setInt(1, bid.getBidderId());
      pst.setInt(2, bid.getAuctionId());
      result = pst.executeUpdate();
    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi delete AuctionParticipation: {}", e.getMessage(), e);
      throw new QueryExecutionException(sql);
    }
    return result;
  }

  public List<Auction> getParticipatedAuctionsByBidder(int bidderId) throws QueryExecutionException {
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
    } catch (SQLException | DatabaseConnectionException e) {
      log.error("Lỗi SQL khi lấy danh sách auction đã tham gia: {}", e.getMessage(), e);
      throw new QueryExecutionException(sql);
    }
    return list;
  }
}