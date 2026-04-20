package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BidTransactionCellController {
    @FXML
    private Label amountLabel;
    @FXML
    private Label bidderNameLabel;
    @FXML
    private Label timestampLabel;

    public void setData(BidTransaction bidTransaction){
        amountLabel.setText(String.valueOf(bidTransaction.getAmount()));
        bidderNameLabel.setText(bidTransaction.getBidderName());
        timestampLabel.setText(bidTransaction.getTimestamp().toString());
    }
}
