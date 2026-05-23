package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidTransactionCell extends ListCell<BidTransaction> {
    private static final Logger log = LoggerFactory.getLogger(BidTransactionCell.class);

    @Override
    protected void updateItem(BidTransaction bidTransaction, boolean empty) {
        super.updateItem(bidTransaction, empty);

        if (empty || bidTransaction == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/com/javfxtutorial/hethongdaugia/view/fxml/BidTransactionCell.fxml"));
            Parent root = loader.load();

            BidTransactionCellController controller = loader.getController();
            controller.setData(bidTransaction);

            setText(null);
            setGraphic(root);
        } catch (Exception e) {
            log.error("Không thể hiển thị lịch sử đặt giá: {}", e.getMessage(), e);
            setGraphic(null);
            setText(
                    bidTransaction.getBidderName() != null
                            ? String.valueOf(bidTransaction.getAmount())
                            : "Khong the hien thi phien dau gia");
        }
    }
}
