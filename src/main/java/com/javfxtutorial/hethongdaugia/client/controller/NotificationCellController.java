package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class NotificationCellController {

    @FXML private HBox   rootHBox;
    @FXML private Label  iconLabel;
    @FXML private Label  msgLabel;
    @FXML private Label  timeLabel;
    @FXML private Label  readBtn;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private Consumer<SellerNotification> onMarkRead;

    public void setOnMarkRead(Consumer<SellerNotification> onMarkRead) {
        this.onMarkRead = onMarkRead;
    }

    public void setData(SellerNotification notif) {
        if (notif == null) return;

        // Màu viền trái theo loại thông báo
        String borderColor = switch (notif.getType()) {
            case CLOSED    -> "#16c784";  // xanh lá
            case PAID      -> "#2980b9";  // xanh dương
            case CANCELLED -> "#dc3545";  // đỏ
        };

        // Màu nền nhạt theo loại (khi chưa đọc), trắng khi đã đọc
        String bgColor = notif.isRead() ? "white" : switch (notif.getType()) {
            case CLOSED    -> "#f0fdf8";  // xanh lá nhạt
            case PAID      -> "#eff6ff";  // xanh dương nhạt
            case CANCELLED -> "#fff5f5";  // đỏ nhạt
        };

        if (rootHBox != null) {
            rootHBox.setStyle(
                    "-fx-background-color: " + bgColor + ";" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: " + borderColor + ";" +
                            "-fx-border-width: 0 0 0 4;" +
                            "-fx-border-radius: 0 12 12 0;" +
                            "-fx-cursor: hand;" +
                            (notif.isRead() ? "-fx-opacity: 0.55;" : "")
            );
            rootHBox.setOnMouseClicked(e -> markRead(notif));
        }

        // Icon loại thông báo
        if (iconLabel != null) {
            iconLabel.setText(switch (notif.getType()) {
                case CLOSED    -> "🏆";
                case PAID      -> "✅";
                case CANCELLED -> "❌";
            });
            iconLabel.setStyle("-fx-font-size: 18px;");
        }

        // Nội dung: đậm + tối khi chưa đọc, mờ khi đã đọc
        if (msgLabel != null) {
            msgLabel.setText(notif.getMessage());
            msgLabel.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: " + (notif.isRead() ? "#aaaaaa" : "#1a1a2e") + ";" +
                            (notif.isRead() ? "" : "-fx-font-weight: bold;")
            );
        }

        if (timeLabel != null) {
            timeLabel.setText(notif.getCreatedAt() != null
                    ? notif.getCreatedAt().format(TIME_FMT) : "");
            // Thời gian cùng màu với viền loại khi chưa đọc
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " +
                    (notif.isRead() ? "#cccccc" : borderColor) + ";");
        }

        // Nút "Đã đọc" — ẩn hoàn toàn, dùng click cả row thay thế
        if (readBtn != null) {
            readBtn.setVisible(false);
            readBtn.setManaged(false);
        }

        // Click vào từng label cũng mark read
        if (msgLabel != null)  msgLabel.setOnMouseClicked(e -> markRead(notif));
        if (iconLabel != null) iconLabel.setOnMouseClicked(e -> markRead(notif));
        if (timeLabel != null) timeLabel.setOnMouseClicked(e -> markRead(notif));
    }

    private void markRead(SellerNotification notif) {
        if (!notif.isRead()) {
            notif.setRead(true);
            if (onMarkRead != null) onMarkRead.accept(notif);
            setData(notif);
        }
    }
}