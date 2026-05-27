package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationListCell extends ListCell<SellerNotification> {
  private static final Logger log = LoggerFactory.getLogger(NotificationListCell.class);

  private Node cellRoot;
  private NotificationCellController cellController;
  private final Consumer<SellerNotification> onMarkRead; // Consumer = truyền dữ hiệu để nó xử lý

  public NotificationListCell(Consumer<SellerNotification> onMarkRead) {
    this.onMarkRead = onMarkRead;
    try {
      FXMLLoader loader =
          new FXMLLoader(
              getClass()
                  .getResource(
                      "/com/javfxtutorial/hethongdaugia/view/fxml/NotificationCellPopup.fxml"));
      cellRoot = loader.load();
      cellController = loader.getController();
      cellController.setOnMarkRead(
          n -> { // Hàm giao vc cho Cell, truyền vào 1 Consumer chứa các bước xử lý tb..
            // ng đọc tác động => Cell lôi đoạn code ra xử lý
            if (onMarkRead != null) onMarkRead.accept(n);
            updateItem(getItem(), false);
          });
    } catch (IOException e) {
      log.error("Không load được NotificationCellPopup.fxml: {}", e.getMessage(), e);
    }
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
