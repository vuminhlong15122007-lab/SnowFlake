package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Controller cho NotificationCellPopup.fxml
 * Được dùng bởi NotificationListCell (ListCell) để hiển thị từng thông báo trong ListView.
 */
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

        boolean isClosed   = notif.getType() == SellerNotification.Type.CLOSED;
        String borderColor = isClosed ? "#16c784" : (notif.getType() == SellerNotification.Type.PAID ? "#2980b9" : "#dc3545");

        if (statusDot != null) {
            statusDot.setFill(notif.isRead() ? Color.TRANSPARENT : Color.web(borderColor));
            statusDot.setVisible(!notif.isRead());
        }

        if (iconLabel != null) {
            iconLabel.setText(switch (notif.getType()) {
                case CLOSED    -> "🏆";
                case PAID      -> "✅";
                case CANCELLED -> "❌";
            });
        }

        if (msgLabel != null) {
            msgLabel.setText(notif.getMessage());
            msgLabel.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: " + (notif.isRead() ? "#888888" : "#1a1a2e") + ";" +
                            (notif.isRead() ? "" : "-fx-font-weight: bold;")
            );
        }

        if (timeLabel != null) {
            timeLabel.setText(notif.getCreatedAt() != null
                    ? notif.getCreatedAt().format(TIME_FMT) : "");
        }

        if (readBtn != null) {
            if (notif.isRead()) {
                readBtn.setVisible(false);
                readBtn.setManaged(false);
            } else {
                readBtn.setVisible(true);
                readBtn.setManaged(true);
                readBtn.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: " + borderColor + ";" +
                                "-fx-cursor: hand; -fx-padding: 3 8 3 8;" +
                                "-fx-background-color: white; -fx-background-radius: 8;" +
                                "-fx-border-color: " + borderColor + "; -fx-border-radius: 8;"
                );
                readBtn.setOnMouseClicked(e -> {
                    notif.setRead(true);
                    if (onMarkRead != null) onMarkRead.accept(notif);
                    setData(notif);
                });
            }
        }
    }
}