package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

public class BidTransactionCell extends ListCell<BidTransaction> {
    @Override
    protected void updateItem(BidTransaction bidTransaction, boolean empty) {
        super.updateItem(bidTransaction, empty);

        if (empty || bidTransaction == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/BidTransactionCell.fxml"));
            Parent root = loader.load();

            BidTransactionCellController controller = loader.getController();
            controller.setData(bidTransaction);

            setText(null);
            setGraphic(root);
        } catch (Exception e) {
            e.printStackTrace();
            setGraphic(null);
            setText(bidTransaction.getBidderName() != null ? String.valueOf(bidTransaction.getAmount()) : "Khong the hien thi phien dau gia");
        }
    }
}
