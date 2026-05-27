package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NotificationCellController {

  @FXML private HBox rootHBox;
  @FXML private Label iconLabel;
  @FXML private Label msgLabel;
  @FXML private Label timeLabel;
  @FXML private Label readBtn;

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

  private Consumer<SellerNotification> onMarkRead;

  public void setOnMarkRead(Consumer<SellerNotification> onMarkRead) {
    this.onMarkRead = onMarkRead;
  }

  public void setData(SellerNotification notif) {
    if (notif == null) return;

    boolean isNoWinner =
        (notif.getType() == SellerNotification.Type.CLOSED)
            && (notif.getWinnerName() == null
                || notif.getWinnerName().isBlank()
                || notif.getWinnerName().equals("N/A")
                || notif.getWinningPrice() == null
                || notif.getWinningPrice().compareTo(java.math.BigDecimal.ZERO) == 0);

    // Màu viền trái theo loại thông báo và trường hợp đặc biệt
    String borderColor;
    if (isNoWinner) {
      borderColor = "#f39c12"; // màu cam cho trường hợp không người thắng
    } else {
      borderColor =
          switch (notif.getType()) {
            case CLOSED -> "#16c784"; // xanh lá (có người thắng)
            case PAID -> "#2980b9"; // xanh dương
            case CANCELLED -> "#dc3545"; // đỏ
            case CANCELLED_BY_ADMIN -> "#e67e22"; // cam
          };
    }

    // Màu nền nhạt theo loại (khi chưa đọc)
    String bgColor;
    if (notif.isRead()) {
      bgColor = "white";
    } else {
      if (isNoWinner) {
        bgColor = "#fff9e6"; // vàng nhạt cho không người thắng
      } else {
        bgColor =
            switch (notif.getType()) {
              case CLOSED -> "#f0fdf8"; // xanh lá nhạt
              case PAID -> "#eff6ff"; // xanh dương nhạt
              case CANCELLED -> "#fff5f5"; // đỏ nhạt
              case CANCELLED_BY_ADMIN -> "#fef9e7"; // cam nhạt
            };
      }
    }

    // Apply style màu cho cả row
    if (rootHBox != null) {
      rootHBox.setStyle(
          "-fx-background-color: "
              + bgColor
              + ";"
              + "-fx-background-radius: 12;"
              + "-fx-border-color: "
              + borderColor
              + ";"
              + "-fx-border-width: 0 0 0 4;"
              + "-fx-border-radius: 0 12 12 0;"
              + "-fx-cursor: hand;"
              + (notif.isRead() ? "-fx-opacity: 0.55;" : ""));
      rootHBox.setOnMouseClicked(e -> markRead(notif));
    }

    // Icon loại thông báo (đổi icon cho trường hợp không người thắng)
    if (iconLabel != null) {
      if (isNoWinner) {
        iconLabel.setText("😞"); // icon mặt buồn
      } else {
        iconLabel.setText(
            switch (notif.getType()) {
              case CLOSED -> "🏆";
              case PAID -> "✅";
              case CANCELLED -> "❌";
              case CANCELLED_BY_ADMIN -> "⚠️";
            });
      }
      iconLabel.setStyle("-fx-font-size: 18px;");
    }

    // Nội dung: đậm + tối khi chưa đọc, mờ khi đã đọc
    if (msgLabel != null) {
      msgLabel.setText(notif.getMessage());
      msgLabel.setStyle(
          "-fx-font-size: 12px;"
              + "-fx-text-fill: "
              + (notif.isRead() ? "#aaaaaa" : "#1a1a2e")
              + ";"
              + (notif.isRead() ? "" : "-fx-font-weight: bold;"));
    }

    if (timeLabel != null) {
      timeLabel.setText(notif.getCreatedAt() != null ? notif.getCreatedAt().format(TIME_FMT) : "");
      timeLabel.setStyle(
          "-fx-font-size: 10px; -fx-text-fill: "
              + (notif.isRead() ? "#cccccc" : borderColor)
              + ";");
    }

    // Nút "Đã đọc" — ẩn hoàn toàn, dùng click cả row thay thế
    if (readBtn != null) {
      readBtn.setVisible(false);
      readBtn.setManaged(false);
    }

    // Click vào từng label cũng mark read
    if (msgLabel != null) msgLabel.setOnMouseClicked(e -> markRead(notif));
    if (iconLabel != null) iconLabel.setOnMouseClicked(e -> markRead(notif));
    if (timeLabel != null) timeLabel.setOnMouseClicked(e -> markRead(notif));
  }

  private void markRead(SellerNotification notif) {
    if (!notif.isRead()) {
      notif.setRead(true);
      ClientModel.getInstance().markNotificationReadByAuction(notif.getAuctionId());
      if (onMarkRead != null) onMarkRead.accept(notif);
      setData(notif);
    }
  }
}
