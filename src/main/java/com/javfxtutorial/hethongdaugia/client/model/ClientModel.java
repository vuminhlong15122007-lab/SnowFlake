package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.Set;

public class ClientModel {
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

    // Lưu id của các thông báo đã đọc trong phiên đăng nhập
    private final Set<Integer> readNotificationIds = new HashSet<>();

    public void markNotificationRead(int notificationId) {
        readNotificationIds.add(notificationId);
    }

    public boolean isNotificationRead(int notificationId) {
        return readNotificationIds.contains(notificationId);
    }

}