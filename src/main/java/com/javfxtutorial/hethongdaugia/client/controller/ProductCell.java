package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class ProductCell extends ListCell<Auction> {
    @Override
    protected void updateItem(Auction auction, boolean empty) {
        super.updateItem(auction, empty);

        if (empty || auction == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/itemphiendaugia.fxml"));
            Parent root = loader.load();

            AuctionSessionController controller = loader.getController();
            controller.setData(auction);

            setText(null);
            setGraphic(root);
        } catch (Exception e) {
            e.printStackTrace();
            setGraphic(null);
            setText(auction.getItem() != null ? auction.getItem().getName() : "Khong the hien thi phien dau gia");
        }
    }
}
