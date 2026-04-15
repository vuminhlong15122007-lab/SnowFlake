package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class Product_qlspSellerController {
    @FXML ImageView imgProduct;
    @FXML Label lbProductName;
    @FXML Label lbPrice;
    @FXML Label ItemID;
    public Item product;

    public void update(Item pr){
        this.product = pr;
        lbPrice.setText(String.format("%,.0f VNĐ", pr.getCurrentPrice()));
        lbProductName.setText(pr.getName());
        ItemID.setText(String.valueOf(pr.getItemId()));
        // SET DU LIEU ANH .... CHUA XU LY

    }





    
}
