package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataDeleteException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataInsertException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DatabaseConnectionException;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationDAO {
    private static final Logger log = LoggerFactory.getLogger(NotificationDAO.class);

    private static volatile NotificationDAO instance;

    private NotificationDAO() {}

    public static NotificationDAO getInstance() {
        if (instance == null) {
            synchronized (NotificationDAO.class) { // BUG FIX: ItemDAO.class → NotificationDAO.class
                if (instance == null) {
                    instance = new NotificationDAO();
                }
            }
        }
        return instance;
    }

    // Priority: PAID > CANCELLED > CLOSED
    public static int priority(SellerNotification.Type type) {
        return switch (type) {
            case PAID -> 2;
            case CANCELLED -> 1;
            case CLOSED -> 0;
        };
    }

    public int insert(SellerNotification notification, int sellerId) throws DataInsertException {
        String sql =
                "INSERT INTO seller_notification"
                        + "(auction_id, seller_id, type, product_name, winner_name, winning_price, is_read, created_at, expires_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)";
        int result = 0;

        try (Connection connection = JDBCUtil.getConnection();
                PreparedStatement pst =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = now.plusDays(2);

            pst.setInt(1, notification.getAuctionId());
            pst.setInt(2, sellerId);
            pst.setString(3, notification.getType().name());
            pst.setString(4, notification.getProductName());
            pst.setString(5, notification.getWinnerName());
            pst.setBigDecimal(6, notification.getWinningPrice());
            pst.setTimestamp(7, Timestamp.valueOf(now));
            pst.setTimestamp(8, Timestamp.valueOf(expires));

            result = pst.executeUpdate();
            if (result > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        notification.setNotificationId(rs.getInt(1));
                    }
                }
                log.info("Tạo Notification thành công, ID: {}", notification.getNotificationId());
            }
        } catch (SQLException | DatabaseConnectionException e) {
            log.error("Lỗi SQL khi insert Notification: {}", e.getMessage(), e);
            throw new DataInsertException("SellerNotification");
        }
        return result;
    }

    public int deleteById(int notificationId) throws DataDeleteException {
        String sql = "DELETE FROM seller_notification WHERE notification_id = ?";
        int result = 0;

        try (Connection connection = JDBCUtil.getConnection();
                PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, notificationId);
            result = pst.executeUpdate();
            if (result > 0) {
                log.info("Đã xóa notification_id={}", notificationId);
            }
        } catch (SQLException | DatabaseConnectionException e) {
            log.error("Lỗi SQL khi delete NotificationId: {}", e.getMessage(), e);
            throw new DataDeleteException(notificationId, "Notification", "delete");
        }
        return result;
    }

    public void insertOrReplace(SellerNotification notification, int sellerId)
            throws DatabaseConnectionException, DataInsertException, DataDeleteException {

        String sql =
                "SELECT notification_id, type FROM seller_notification"
                        + " WHERE auction_id = ? AND seller_id = ? LIMIT 1";

        try (Connection connection = JDBCUtil.getConnection();
                PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, notification.getAuctionId());
            pst.setInt(2, sellerId);

            try (ResultSet rs = pst.executeQuery()) { // BUG FIX: executeQuery thay executeUpdate
                if (rs.next()) {
                    int existingId = rs.getInt("notification_id");
                    String typeStr = rs.getString("type");
                    SellerNotification.Type existingType = SellerNotification.Type.valueOf(typeStr);

                    if (priority(notification.getType()) > priority(existingType)) {
                        deleteById(existingId);
                        insert(notification, sellerId);
                        log.info(
                                "Đã thay thế notification id={} ({} -> {})",
                                existingId,
                                existingType,
                                notification.getType());
                    } else {
                        log.info(
                                "Giữ nguyên notification cũ id={} (priority {} >= {})",
                                existingId,
                                priority(existingType),
                                priority(notification.getType()));
                    }
                } else {
                    // BUG FIX: nhánh else bị thiếu - chưa có notification -> insert mới
                    insert(notification, sellerId);
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            log.error("Lỗi insertOrReplace notification: {}", e.getMessage(), e);
        }
    }

    public List<SellerNotification> findBySellerId(int sellerId) {
        String sql =
                "SELECT notification_id, auction_id, type, product_name, winner_name,"
                        + " winning_price, is_read, created_at"
                        + " FROM seller_notification"
                        + " WHERE seller_id = ? AND expires_at > NOW()"
                        + " ORDER BY is_read ASC, created_at DESC";

        List<SellerNotification> list = new ArrayList<>();

        try (Connection conn = JDBCUtil.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, sellerId);

            try (ResultSet rs =
                    pst.executeQuery()) { // BUG FIX: bỏ executeUpdate, dùng executeQuery
                while (rs.next()) {
                    SellerNotification n =
                            new SellerNotification(
                                    rs.getInt("auction_id"),
                                    SellerNotification.Type.valueOf(rs.getString("type")),
                                    rs.getString("product_name"),
                                    rs.getString("winner_name"),
                                    rs.getBigDecimal("winning_price"));
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setRead(rs.getInt("is_read") == 1);
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
                    list.add(n);
                }
            }
            log.info("Lấy {} thông báo cho seller_id={}", list.size(), sellerId);

        } catch (SQLException | DatabaseConnectionException e) {
            log.error("Lỗi findBySellerId: {}", e.getMessage(), e);
        }
        return list;
    }

    public int markAsRead(int notificationId) {
        String sql = "UPDATE seller_notification SET is_read = 1 WHERE notification_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, notificationId);
            int result = pst.executeUpdate();
            if (result > 0) {
                log.info("Đã đọc notification_id={}, rows={}", notificationId, result);
            }
            return result;

        } catch (SQLException | DatabaseConnectionException e) {
            log.error("Lỗi markAsRead: {}", e.getMessage(), e);
            return 0;
        }
    }
}
