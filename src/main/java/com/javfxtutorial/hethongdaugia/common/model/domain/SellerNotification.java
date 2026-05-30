package com.javfxtutorial.hethongdaugia.common.model.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SellerNotification implements Serializable {
  private static int idCounter = 0;

  public enum Type {
    CLOSED,
    PAID,
    CANCELLED,
    CANCELLED_BY_ADMIN
  }

  private int notificationId;
  private int auctionId;
  private Type type;
  private String productName;
  private String winnerName;
  private BigDecimal winningPrice;
  private LocalDateTime createdAt;
  private boolean read;
  private LocalDateTime closedAt; // thời điểm đạt trạng thái cuối

  public SellerNotification() {}

  public SellerNotification(
      int auctionId, Type type, String productName, String winnerName, BigDecimal winningPrice) {
    idCounter += 1;
    this.auctionId = auctionId;
    this.type = type;
    this.productName = productName;
    this.winnerName = winnerName;
    this.winningPrice = winningPrice;
    this.createdAt = LocalDateTime.now();
    this.read = false;
    this.notificationId = idCounter;
    boolean isFinal =
        switch (type) {
          case CANCELLED, PAID, CANCELLED_BY_ADMIN -> true;
          case CLOSED -> winnerName == null || winnerName.isBlank() || winnerName.equals("N/A");
        };
    this.closedAt = isFinal ? LocalDateTime.now() : null;
  }

  public LocalDateTime getClosedAt() {
    return closedAt;
  }

  public int getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(int v) {
    notificationId = v;
  }

  public int getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(int v) {
    auctionId = v;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;

    // Cập nhật lại closedAt khi type thay đổi
    boolean isFinal =
        switch (type) {
          case CANCELLED, PAID, CANCELLED_BY_ADMIN -> true;
          case CLOSED -> winnerName == null || winnerName.isBlank() || winnerName.equals("N/A");
        };

    if (isFinal && this.closedAt == null) {
      this.closedAt = LocalDateTime.now(); // chỉ set lần đầu, không ghi đè
    }
  }

  public String getProductName() {
    return productName;
  }

  public String getWinnerName() {
    return winnerName;
  }

  public BigDecimal getWinningPrice() {
    return winningPrice;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime v) {
    createdAt = v;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public String getMessage() {
    return switch (type) {
      case CLOSED -> {
        boolean noWinner = winnerName == null || winnerName.isBlank() || winnerName.equals("N/A");
        if (noWinner) {
          yield String.format(
              "😞 Phiên \"%s\" kết thúc!\n Không có ai tham gia đấu giá.", productName);
        } else {
          String price = (winningPrice != null) ? String.format("%,.0f", winningPrice) : "—";
          yield String.format(
              "🏆 Phiên \"%s\" kết thúc!\nNgười thắng: %s — Giá: %s VND\nĐang chờ họ thanh toán.",
              productName, winnerName, price);
        }
      }
      case PAID -> {
        String price = String.format("%,.0f", winningPrice);
        yield String.format(
            "✅ Phiên \"%s\" đã được thanh toán!\n \"%s\"  đã thanh toán thành công  \"%s\" VND.\nChuẩn bị giao hàng cho họ nhé!",
            productName, winnerName, price);
      }
      case CANCELLED ->
          String.format(
              "❌ Phiên \"%s\" bị hủy!\n \"%s\" không thanh toán trong 24h.\nBạn có thể kiện nếu cần.",
              productName, winnerName);

      case CANCELLED_BY_ADMIN ->
          String.format(
              "⚠️ Phiên đấu giá \"%s\" đã bị admin hủy!\n Lý do: Phiên có dấu nghi vấn gian lận hoặc \n vi phạm tiêu chuẩn cộng đồng",
              productName);
    };
  }
}
