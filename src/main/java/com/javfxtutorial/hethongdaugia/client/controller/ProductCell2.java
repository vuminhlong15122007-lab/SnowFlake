package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductCell2 extends ListCell<Auction> {
    private static final Logger log = LoggerFactory.getLogger(ProductCell2.class);

    @Override
    protected void updateItem(Auction auction, boolean empty) {
        super.updateItem(auction, empty);

        if (empty || auction == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductCell.fxml"));
            Parent root = loader.load();

            SellerProductController cellController = loader.getController();
            cellController.update(auction);

            setText(null);
            setGraphic(root);
        } catch (Exception e) {
            log.error("Không thể hiển thị sản phẩm: {}", e.getMessage(), e);
            setGraphic(null);
            setText(
                    auction.getItem() != null
                            ? auction.getItem().getName()
                            : "Khong the hien thi san pham");
        }
    }
}
