package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class NotificationCellController {

    @FXML private Circle statusDot;
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

        String borderColor = switch (notif.getType()) {
            case CLOSED    -> "#16c784";
            case PAID      -> "#2980b9";
            case CANCELLED -> "#dc3545";
        };

        // Icon loại thông báo
        if (iconLabel != null) {
            iconLabel.setText(switch (notif.getType()) {
                case CLOSED    -> "🏆";
                case PAID      -> "✅";
                case CANCELLED -> "❌";
            });
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
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " +
                    (notif.isRead() ? "#cccccc" : "#9b9b9b") + ";");
        }

        // Nút "Đã đọc" — ẩn hoàn toàn, dùng click cả row thay thế
        if (readBtn != null) {
            readBtn.setVisible(false);
            readBtn.setManaged(false);
        }

        // Click vào cả row để mark read
        if (msgLabel != null) {
            msgLabel.setOnMouseClicked(e -> markRead(notif));
        }
        if (iconLabel != null) {
            iconLabel.setOnMouseClicked(e -> markRead(notif));
        }
        if (timeLabel != null) {
            timeLabel.setOnMouseClicked(e -> markRead(notif));
        }
    }

    private void markRead(SellerNotification notif) {
        if (!notif.isRead()) {
            notif.setRead(true);
            if (onMarkRead != null) onMarkRead.accept(notif);
            setData(notif);
        }
    }
}