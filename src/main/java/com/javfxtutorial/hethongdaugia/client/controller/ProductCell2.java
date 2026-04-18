package com.javfxtutorial.hethongdaugia.client.controller;

   // man hinh de product_qlspSeller co the hien thi trong quan_ly_san_pham_seller

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class ProductCell2 extends ListCell<Auction> {
    @Override
    protected void updateItem(Auction auction, boolean empty) {  // method cua class cha da la protected
        super.updateItem(auction, empty);
        if (empty || auction == null) {  // Tuc la man hinh da khong hien thi sp nua . tinh nang cua ListView
            setText(null); // lam null chu de tai sd lai cai itemphien... y
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/product_qlspSeller.fxml"));
                Parent root = loader.load();

                // Lấy controller của cell
                SellerProductController cellController = loader.getController();
                // Gọi phương thức update để truyền dữ liệu
                cellController.update(auction);

                setGraphic(root);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}


