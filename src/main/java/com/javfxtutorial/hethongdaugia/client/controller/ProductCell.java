package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

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
            String fxmlFile;
            fxmlFile = "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionCell.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            AuctionSessionController controller = loader.getController();
            controller.setData(auction);
            setText(null);
            setGraphic(root);
        } catch (Exception e) {
            System.err.println("LỖI LOAD CELL: " + e.getMessage());
            e.printStackTrace();
            setGraphic(null);
            setText(auction.getItem() != null ? auction.getItem().getName() : "Lỗi");
        }
    }
}