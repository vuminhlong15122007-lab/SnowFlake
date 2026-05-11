package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.common.model.Auction;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ParticipatedAuctionCellController {
    @FXML private Button actionButton;
    @FXML private Label lbCategory;
    @FXML private Label lbCurrentPrice;
    @FXML private Label lbProductName;
    @FXML private Label lbWinnerName;
    @FXML private ImageView productImage;

    private Auction auction;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) return;
        this.auction = auction;

        // Thông tin chung
        lbProductName.setText(auction.getItem().getName());
        lbCategory.setText(String.valueOf(auction.getItem().getCategory()));
        lbWinnerName.setText(auction.getWinnerId() != 0 ? String.valueOf(auction.getWinnerId()) : "Không có người đấu giá");
        lbCurrentPrice.setText(String.valueOf(auction.getCurrentPrice()));
        // Load ảnh
        if (!(productImage == null || auction.getItem().getImage() == null || auction.getItem().getImage().isBlank())) {
            ImageHelper.loadBase64ToImageView(productImage, auction.getItem().getImage());
        }
        ;
        /// CHUS YS CAI NAY CHUA XONG .... KHONG PHAI GOI AUCTION.GETSTATUS()???
        switch (auction.getStatus()) {
            case RUNNING:
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setText("THAM GIA");
                    actionButton.setStyle("-fx-background-color: linear-gradient(to right, #56ccf2, #2f80ed); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");

                }

                if (lbWinnerName != null) lbWinnerName.setText("" + auction.getWinnerId());
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                break;

            case NOT_START:
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getInitPrice()));
                if (actionButton != null) {
                    actionButton.setText("CHƯA BẮT ĐẦU");
                    actionButton.setStyle("-fx-background-color: linear-gradient(to right, red, #f39c12); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
                }
                lbWinnerName.setText(null);
                break;

            case CLOSED:
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getWinningPrice()));
                lbCategory.setText("Loại: " + (auction.getItem().getCategory() != null ? auction.getItem().getCategory() : "Khác"));
                if (lbWinnerName != null) lbWinnerName.setText("" + auction.getWinnerId());
                break;
        }


    }
}
