package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class SellerProductController {
    @FXML ImageView imgProduct;
    @FXML Label lbProductName;
    @FXML Label lbPrice;
    @FXML Label ItemID;


    public void update(Auction auction){
        lbPrice.setText(String.format("%,.0f VNĐ", auction.getCurrentPrice()));
        lbProductName.setText(auction.getItem().getName());
        ItemID.setText(String.valueOf(auction.getItem().getItemId()));
        // SET DU LIEU ANH .... CHUA XU LY

    }





    
}
