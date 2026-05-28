package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductCell extends ListCell<Auction> {
  private static final Logger log = LoggerFactory.getLogger(ProductCell.class);

  @Override
  protected void updateItem(Auction auction, boolean empty) {
    super.updateItem(auction, empty);

    if (empty || auction == null) {
      setText(null);
      setGraphic(null);
      return;
    }

    try {
      String fxmlFile;
      fxmlFile = "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionCell.fxml";
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
      Parent root = loader.load();
      AuctionListCellController controller = loader.getController();
      controller.setData(auction);
      setText(null);
      setGraphic(root);
    } catch (Exception e) {
      log.error("LỖI LOAD CELL: {}", e.getMessage(), e);
      setGraphic(null);
      setText(auction.getItem() != null ? auction.getItem().getName() : "Lỗi");
    }
  }
}
