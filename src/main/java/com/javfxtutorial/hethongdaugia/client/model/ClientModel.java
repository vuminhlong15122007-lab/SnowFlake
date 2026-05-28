package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final ObservableList<Auction> allAuctions = FXCollections.observableArrayList();

  public ObservableList<Auction> getAllAuctions() {
    return allAuctions;
  }

  private final ObservableList<Auction> myAuctions = FXCollections.observableArrayList();

  public ObservableList<Auction> getMyAuctions() {
    return myAuctions;
  }

  private final ObservableList<SellerNotification> sellerNotifications =
      FXCollections.observableArrayList();

  public ObservableList<SellerNotification> getSellerNotifications() {
    return sellerNotifications;
  }

  private ScheduledExecutorService
      pruneScheduler; // ScheduledExecutorService là interface con của ExecutorService, có thêm khả
  // năng chạy task theo lịch

  /*
   Preferences — giúp lưu lại trạng thái của user, khi tắt app vẫn còn. Giống DB but chỉ dành cho dữ liệu nhỏ.
  Lưu dạng key-value vào disk theo hệ điều hành.Ở đây dùng để lưu: auctionId nào đã đọc + thời điểm đọc (epochSecond).
  */

  private Map<Integer, Long> readNotificationIds = new HashMap<>();

  public void loadReadNotificationIds(int userId) {
    readNotificationIds = new HashMap<>();
    try {
      // Mỗi user có 1 node riêng biệt
      Preferences prefs =
          Preferences.userRoot().node("com/javfxtutorial/hethongdaugia/notifications/" + userId);

      String raw = prefs.get("read_ids", "");

      if (!raw.isEmpty()) {
        for (String s : raw.split(",")) {
          try {
            String[] parts = s.trim().split(":");
            int auctionId = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            readNotificationIds.put(auctionId, timestamp);
          } catch (Exception ignored) {
            log.warn("Invalid read_id entry: {}", s.trim());
          }
        }
      }

      // Dọn dẹp ngay khi load: xóa các notification đã quá 2 ngày ở trạng thái cuối
      pruneExpiredReadIds();

    } catch (Exception e) {
      log.error("Lỗi load Preferences (userId={}): {}", userId, e.getMessage(), e);
    }
  }

  public void pruneExpiredReadIds() {
    // 1. Tính mốc thời gian cách đây đúng 2 ngày (tính bằng số giây)
    long currentSeconds = Instant.now().getEpochSecond();
    long twoDaysAgo = currentSeconds - 2L * 24 * 60 * 60;

    Set<Integer> expiredIds = new HashSet<>();

    for (SellerNotification n : sellerNotifications) {
      if (this.isFinalState(n)) { // phải ở tthai cuối
        if (n.getClosedAt() != null) {
          long closedAtSeconds = n.getClosedAt().toEpochSecond(ZoneOffset.UTC);
          if (closedAtSeconds < twoDaysAgo) { // tbao phải quá 2 ngày ...
            Integer auctionId = n.getAuctionId();
            expiredIds.add(auctionId);
          }
        }
      }
    }

    // Xóa các ID hết hạn ra khỏi readNotificationIds
    for (Integer idToRemove : expiredIds) {
      readNotificationIds.remove(idToRemove);
    }
    persistReadIds(); // xóa trong disk , UI vẫn hiện nhưng đã óatrong disk rồi
  }

  private boolean isFinalState(SellerNotification n) {
    return switch (n.getType()) {
      case CANCELLED, PAID, CANCELLED_BY_ADMIN -> true;
      case CLOSED -> {
        boolean noWinner = n.getWinnerName() == null || n.getWinnerName().isBlank();
        yield noWinner;
      }
    };
  }

  // KTr notification của 1 auction đã đc đọc chx.
  public boolean isNotificationReadByAuction(int auctionId) {
    return readNotificationIds.containsKey(auctionId);
  }

  /**
   * Đánh dấu notification của 1 auction là đã đọc. Lưu cả thời điểm đọc để sau 2 ngày có thể tự
   * động xóa.
   */
  public void markNotificationReadByAuction(int auctionId) {
    long now = Instant.now().getEpochSecond();
    readNotificationIds.put(auctionId, now); // thêm vào RAM kèm timestamp
    persistReadIds(); // ghi xuống disk ngay lập tức
  }

  /**
   * Đánh dấu lại notification là Chx đọc. Dùng khi notification được nâng lên priority cao hơn (ví
   * dụ: CLOSED → PAID hoặc CLOSED → CANCELLED) để seller thấy thông báo mới.
   */
  public void markNotificationUnread(int auctionId) {
    readNotificationIds.remove(auctionId);
    persistReadIds();
  }

  private void persistReadIds() {
    if (currentUser == null) {
      return;
    }

    try {
      //  Tìm hoặc tạo "thư mục riêng" trên ổ cứng cho người dùng này
      String userFolder = "com/javfxtutorial/hethongdaugia/notifications/" + currentUser.getId();
      Preferences prefs = Preferences.userRoot().node(userFolder);

      // Chuyển dữ liệu của Map thành một chuỗi chữ (String) dài với form
      // auctionId:timestamp,auctionId:timestamp
      StringBuilder stringBuilder = new StringBuilder();

      for (Map.Entry<Integer, Long> entry : readNotificationIds.entrySet()) {
        Integer auctionId = entry.getKey();
        Long timestamp = entry.getValue();

        if (stringBuilder.length() > 0) {
          stringBuilder.append(",");
        }

        stringBuilder.append(auctionId);
        stringBuilder.append(":");
        stringBuilder.append(timestamp);
      }

      // Chuyển toàn bộ kết quả trong StringBuilder thành một chuỗi String bình thường
      String joined = stringBuilder.toString();

      prefs.put("read_ids", joined);

      prefs.flush(); // đẩy xuống disk

    } catch (BackingStoreException e) {
      log.warn("Không thể ghi trạng thái đã đọc vào Preferences: " + e.getMessage());

    } catch (Exception e) {
      log.error("Lỗi ghi Preferences: " + e.getMessage(), e);
    }
  }

  public void startPruneScheduler() {
    pruneScheduler = Executors.newSingleThreadScheduledExecutor();
    pruneScheduler.scheduleAtFixedRate(
        () -> Platform.runLater(this::pruneExpiredReadIds),
        1,
        1,
        TimeUnit.HOURS // chờ 1 tiếng rồi mới chạy lần đầu, sau đó mỗi 1 tiếng 1 lần
        );
  }

  public void stopPruneScheduler() {
    if (pruneScheduler != null && !pruneScheduler.isShutdown()) {
      pruneScheduler.shutdown();
    }
  }

  public void logout() {
    stopPruneScheduler();
    currentUser = null;
    currentAuction = null;
    sellerNotifications.clear();
    readNotificationIds.clear(); // chỉ xóa RAM, disk vẫn còn
    myAuctions.clear();
    NetworkManager.getInstance().clearAllListeners();
  }
}
