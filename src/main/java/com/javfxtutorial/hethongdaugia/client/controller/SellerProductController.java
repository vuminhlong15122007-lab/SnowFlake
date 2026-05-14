package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import static com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus.NOT_START;
import static com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus.RUNNING;

public class SellerProductController {
    @FXML private ImageView itemImageView1;
    @FXML private Label lbProductName;
    @FXML private Label lbPrice;
    @FXML private Label ItemID;
    @FXML private Label lbStatus;


    public void update(Auction auction){
        lbPrice.setText(String.format("%,.0f VNĐ", auction.getCurrentPrice()));
        lbProductName.setText(auction.getItem().getName());
        ItemID.setText(String.valueOf(auction.getItem().getItemId()));
        String base64Data = auction.getItem().getImage();
        ImageHelper.loadBase64ToImageView(itemImageView1 , base64Data);
        AuctionStatus status = auction.getStatus();

        if (status == NOT_START){
            lbStatus.setStyle("-fx-text-fill: orange; -fx-font-size : 18px;");
        }
        else if(status == RUNNING){
            lbStatus.setStyle("-fx-text-fill: green; -fx-font-size : 18px;");
        }else{
            lbStatus.setStyle("-fx-text-fill: red; -fx-font-size : 18px;");
        }
        makeElementFlash(lbStatus);
    }
    // hiệu ứng nháy nháy nè
    public void makeElementFlash(Node element) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), element);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.1);
        fadeTransition.setCycleCount(Animation.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
        }
}

