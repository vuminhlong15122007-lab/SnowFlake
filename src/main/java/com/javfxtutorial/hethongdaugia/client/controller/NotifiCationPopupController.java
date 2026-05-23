package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class NotifiCationPopupController {

    @FXML private ListView<SellerNotification> notificationListView;
    @FXML private Label emptyLabel;

    // gọi lại khi một thông báo được đánh dấu đã đọc
    private Consumer<SellerNotification> onMarkRead;

    public void setOnMarkRead(Consumer<SellerNotification> onMarkRead) {
        this.onMarkRead = onMarkRead;
    }

    @FXML
    public void initialize() {
        if (notificationListView != null) {
            notificationListView.setCellFactory(
                    _ -> new NotificationListCell(notif -> {
                                        if (onMarkRead != null) onMarkRead.accept(notif);
                                        // Refresh listview để cell tự cập nhật trạng thái
                                        Platform.runLater(() -> notificationListView.refresh());
                                    }));
            notificationListView.setStyle("-fx-background-color: transparent;"
                            + "-fx-background-insets: 0;"
                            + "-fx-control-inner-background: transparent;");
        }
    }

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
