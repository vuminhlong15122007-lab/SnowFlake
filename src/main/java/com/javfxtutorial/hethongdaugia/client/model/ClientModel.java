package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

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

    private final ObservableList<Auction> allAuctions =
        FXCollections.observableArrayList();
    public ObservableList<Auction> getAllAuctions(){
        return allAuctions;
    }
    private final ObservableList<Auction> joinedAuctions =
        FXCollections.observableArrayList();
    public ObservableList<Auction> getJoinedAuctions(){
        return joinedAuctions;
    }
    private final ObservableList<Auction> myAuctions =
        FXCollections.observableArrayList();
    public ObservableList<Auction> getMyAuctions(){
        return myAuctions;
    }
    // ── Seller notifications — tồn tại suốt phiên đăng nhập ──────────────────
    // Dùng ObservableList để các controller có thể lắng nghe thay đổi
    private final ObservableList<SellerNotification> sellerNotifications =
            FXCollections.observableArrayList();

    /** Trả về list thông báo seller — dùng chung cho mọi lần vào màn hình seller */
    public ObservableList<SellerNotification> getSellerNotifications() {
        return sellerNotifications;
    }

    /** Xóa hết thông báo khi user logout */
    public void clearSellerNotifications() {
        sellerNotifications.clear();
    }
}