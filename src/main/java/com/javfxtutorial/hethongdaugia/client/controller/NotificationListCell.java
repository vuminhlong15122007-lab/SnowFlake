package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.SellerNotification;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * ListCell<SellerNotification> dùng NotificationCellPopup.fxml.
 * Không dùng HBox xây tay — render qua FXML + NotificationCellController.
 */
public class NotificationListCell extends ListCell<SellerNotification> {

    private Node cellRoot;
    private NotificationCellController cellController;
    private final Consumer<SellerNotification> onMarkRead;

    public NotificationListCell(Consumer<SellerNotification> onMarkRead) {
        this.onMarkRead = onMarkRead;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/javfxtutorial/hethongdaugia/view/fxml/NotificationCellPopup.fxml"));
            cellRoot = loader.load();
            cellController = loader.getController();
            cellController.setOnMarkRead(n -> {
                // Gọi callback ngoài rồi refresh cell
                if (onMarkRead != null) onMarkRead.accept(n);
                updateItem(getItem(), false);
            });
        } catch (IOException e) {
            System.err.println("Không load được NotificationCellPopup.fxml: " + e.getMessage());
        }
        // Không có padding nền mặc định của ListCell
        setStyle("-fx-background-color: transparent; -fx-padding: 4 8 4 8;");
    }

    @Override
    protected void updateItem(SellerNotification item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || cellRoot == null) {
            setGraphic(null);
        } else {
            cellController.setData(item);
            setGraphic(cellRoot);
        }
    }
}