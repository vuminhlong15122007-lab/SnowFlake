package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class BidTransactionCellController {
    @FXML private Label amountLabel;
    @FXML private Label bidderNameLabel;
    @FXML private Label timestampLabel;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public void setData(BidTransaction bidTransaction) {
        amountLabel.setText(String.format("%,.0f VND", bidTransaction.getAmount()));
        bidderNameLabel.setText(bidTransaction.getBidderName());
        timestampLabel.setText(bidTransaction.getTimestamp().format(TIME_FMT));
    }
}
