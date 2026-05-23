package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.AuctionModificationManager;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;

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

    // ── Session ───────────────────────────────────────────────────────────────
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private Auction currentAuction;

    public Auction getCurrentAuction() {
        return currentAuction;
    }

    public void setCurrentAuction(Auction currentAuction) {
        this.currentAuction = currentAuction;
    }

    // ── Observable lists ──────────────────────────────────────────────────────
    private final ObservableList<Auction> allAuctions = FXCollections.observableArrayList();

    public ObservableList<Auction> getAllAuctions() {
        return allAuctions;
    }

    private final ObservableList<Auction> joinedAuctions = FXCollections.observableArrayList();

    public ObservableList<Auction> getJoinedAuctions() {
        return joinedAuctions;
    }

    private final ObservableList<Auction> myAuctions = FXCollections.observableArrayList();

    public ObservableList<Auction> getMyAuctions() {
        return myAuctions;
    }

    // ── Seller notifications ──────────────────────────────────────────────────
    private final ObservableList<SellerNotification> sellerNotifications =
            FXCollections.observableArrayList();

    public ObservableList<SellerNotification> getSellerNotifications() {
        return sellerNotifications;
    }

    // ── Read notification IDs (persist qua Preferences) ───────────────────────
    private Set<Integer> readNotificationIds = new HashSet<>();

    public void loadReadNotificationIds(int userId) {
        readNotificationIds = new HashSet<>();
        try {
            java.util.prefs.Preferences prefs =
                    java.util.prefs.Preferences.userRoot()
                            .node("com/javfxtutorial/hethongdaugia/notifications/" + userId);
            String raw = prefs.get("read_ids", "");
            if (!raw.isEmpty()) {
                for (String s : raw.split(",")) {
                    try {
                        readNotificationIds.add(Integer.parseInt(s.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi load Preferences (userId={}): {}", userId, e.getMessage(), e);
        }
    }

    public void markNotificationRead(int notificationId) {
        readNotificationIds.add(notificationId);
        persistReadIds();
    }

    public boolean isNotificationRead(int notificationId) {
        return readNotificationIds.contains(notificationId);
    }

    public boolean isNotificationReadByAuction(int auctionId) {
        return readNotificationIds.contains(auctionId);
    }

    public void markNotificationReadByAuction(int auctionId) {
        readNotificationIds.add(auctionId);
        persistReadIds();
    }

    /** Đánh dấu lại CHƯA đọc — dùng khi notification được nâng type (CLOSED→PAID/CANCELLED) */
    public void markNotificationUnread(int auctionId) {
        readNotificationIds.remove(auctionId);
        persistReadIds();
    }

    /** Xóa RAM cache (không xóa disk — giữ cho lần đăng nhập sau) */
    public void clearReadNotificationIds() {
        readNotificationIds.clear();
    }


    public void removeMyAuctionById(int auctionId) {
        myAuctions.removeIf(a -> a.getAuctionId() == auctionId);
    }

    private void persistReadIds() {
        if (currentUser == null) return;
        try {
            Preferences prefs = Preferences.userRoot().node("com/javfxtutorial/hethongdaugia/notifications/" + currentUser.getId());
            String joined = readNotificationIds.stream().map(String::valueOf).collect(joining(","));
            prefs.put("read_ids", joined);
            prefs.flush();
        } catch (java.util.prefs.BackingStoreException e) {
            log.warn("Không thể ghi trạng thái đã đọc vào Preferences: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi ghi Preferences: {}", e.getMessage(), e);
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    public void logout() {
        currentUser = null;
        currentAuction = null;
        sellerNotifications.clear();
        readNotificationIds.clear();
        myAuctions.clear();
        AuctionModificationManager.getInstance().isAllAuctionsLoaded = false;
        getAllAuctions().clear();
    }
}