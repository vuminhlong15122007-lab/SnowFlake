package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


public class ClientModel {
    private static final Logger log = LoggerFactory.getLogger(ClientModel.class);
    private static ClientModel instance;
    private ClientModel() {}

    public static ClientModel getInstance() {
        if (instance == null) {
            instance = new ClientModel();
        }
        return instance;
    }

    private User currentUser;
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    private Auction currentAuction;
    public Auction getCurrentAuction() { return currentAuction; }
    public void setCurrentAuction(Auction currentAuction) { this.currentAuction = currentAuction; }

    private Item currentItem;
    public Item getCurrentItem() { return currentItem; }
    public void setCurrentItem(Item currentItem) { this.currentItem = currentItem; }

    // ── Seller notifications — tồn tại suốt phiên đăng nhập
    // Dùng ObservableList để các controller có thể lắng nghe thay đổi
    private final ObservableList<SellerNotification> sellerNotifications =
            FXCollections.observableArrayList();

    public ObservableList<SellerNotification> getSellerNotifications() {
        return sellerNotifications;
    }

    public void clearSellerNotifications() {
        sellerNotifications.clear();
    }


    private Set<Integer> readNotificationIds = new HashSet<>();


    public void loadReadNotificationIds(int userId) {
        readNotificationIds = new HashSet<>();
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences
                    .userRoot()
                    .node("com/javfxtutorial/hethongdaugia/notifications/" + userId);
            String raw = prefs.get("read_ids", "");
            if (!raw.isEmpty()) {
                for (String s : raw.split(",")) {
                    try { readNotificationIds.add(Integer.parseInt(s.trim())); }
                    catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            log.error("Lỗi không xác định khi load Preferences (userId={}): {}", userId, e.getMessage(), e);
        }
    }


    public void markNotificationRead(int notificationId) {
        readNotificationIds.add(notificationId);
        persistReadIds();
    }

    public boolean isNotificationRead(int notificationId) {
        return readNotificationIds.contains(notificationId);
    }

    /** Alias dùng auctionId — gọi giống isNotificationRead nhưng tên rõ hơn */
    public boolean isNotificationReadByAuction(int auctionId) {
        return readNotificationIds.contains(auctionId);
    }

    public void markNotificationReadByAuction(int auctionId) {
        readNotificationIds.add(auctionId);
        persistReadIds();
    }

    /** Đánh dấu lại CHƯA đọc — dùng khi notification được nâng lên type cao hơn (CLOSED→PAID/CANCELLED) */
    public void markNotificationUnread(int auctionId) {
        readNotificationIds.remove(auctionId);
        persistReadIds();
    }

    /** Xóa RAM cache (không xóa disk — dữ liệu disk giữ cho lần đăng nhập sau) */
    public void clearReadNotificationIds() {
        readNotificationIds.clear();
    }

    private void persistReadIds() {
        if (currentUser == null) return;
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences
                    .userRoot()
                    .node("com/javfxtutorial/hethongdaugia/notifications/" + currentUser.getId());
            String joined = readNotificationIds.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
            prefs.put("read_ids", joined);
            prefs.flush();
        } catch (java.util.prefs.BackingStoreException e) {
            log.warn("Không thể ghi trạng thái đã đọc vào Preferences: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi không xác định khi ghi Preferences: {}", e.getMessage(), e);
        }
    }


    public void logout() {
        currentUser    = null;
        currentAuction = null;
        currentItem    = null;
        sellerNotifications.clear();
        readNotificationIds.clear();
    }

}