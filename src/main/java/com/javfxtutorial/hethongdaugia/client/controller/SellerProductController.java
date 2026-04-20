package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class SellerProductController {
    @FXML private ImageView itemImageView1;
    @FXML private Label lbProductName;
    @FXML private Label lbPrice;
    @FXML private Label ItemID;


    public void update(Auction auction){
        lbPrice.setText(String.format("%,.0f VNĐ", auction.getCurrentPrice()));
        lbProductName.setText(auction.getItem().getName());
        ItemID.setText(String.valueOf(auction.getItem().getItemId()));
        String base64Data = auction.getItem().getImage();
        ImageHelper.loadBase64ToImageView(itemImageView1 , base64Data);
    }





    
}
