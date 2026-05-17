package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.function.Consumer;

/**
 * Controller của NotifiCationPopup.fxml.
 * Sử dụng ListView<SellerNotification> + NotificationListCell (FXML cell).
 * Không xây HBox tay nữa.
 */
public class NotifiCationPopupController {

    @FXML private ListView<SellerNotification> notificationListView;
    @FXML private Label emptyLabel;

    /** Callback gọi lại khi một thông báo được đánh dấu đã đọc */
    private Consumer<SellerNotification> onMarkRead;

    public void setOnMarkRead(Consumer<SellerNotification> onMarkRead) {
        this.onMarkRead = onMarkRead;
    }

    @FXML
    public void initialize() {
        if (notificationListView != null) {
            notificationListView.setCellFactory(_ ->
                    new NotificationListCell(notif -> {
                        if (onMarkRead != null) onMarkRead.accept(notif);
                        // Refresh listview để cell tự cập nhật trạng thái
                        Platform.runLater(() -> notificationListView.refresh());
                    })
            );
            notificationListView.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-insets: 0;" +
                            "-fx-control-inner-background: transparent;"
            );
        }
    }

    /**
     * Được gọi từ SellerManagementController sau khi FXML load xong.
     * Truyền thẳng ObservableList — ListView sẽ tự cập nhật khi list thay đổi.
     */
    public void loadNotifications(ObservableList<SellerNotification> notifications) {
        if (notificationListView == null) return;

        notificationListView.setItems(notifications);

        boolean empty = (notifications == null || notifications.isEmpty());
        if (emptyLabel != null) {
            emptyLabel.setVisible(empty);
            emptyLabel.setManaged(empty);
        }
        notificationListView.setVisible(!empty);
        notificationListView.setManaged(!empty);
    }
}