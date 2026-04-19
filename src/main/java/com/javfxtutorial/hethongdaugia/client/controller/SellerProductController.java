package com.javfxtutorial.hethongdaugia.client.controller;

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
        if (base64Data == null || base64Data.isBlank()) {
            if (itemImageView1 != null) {
                itemImageView1.setImage(null);
            }
            return;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            if (itemImageView1 != null) {
                itemImageView1.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Loi load anh: " + e.getMessage());
            if (itemImageView1 != null) {
                itemImageView1.setImage(null);
            }
        }
    }





    
}
