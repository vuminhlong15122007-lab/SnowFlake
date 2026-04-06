package com.javfxtutorial.hethongdaugia.client.controller;

import com.sun.source.tree.TryTree;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import com.javfxtutorial.hethongdaugia.common.model.Item;

public class ItemphiendaugiaController {
    @FXML Label lbAuctionName;
    @FXML Label lbGiaSp;
    @FXML Button btnDauGia;
    @FXML Label lbLuotDau;  // 2 CAI Label cuoi chx xu ly duoc
    @FXML Label lbTimer;
    @FXML ImageView imgSanPham;

    private Item product;

    public void setData(Item product){   // xu ly du lieu tu Obj den giao dien
        this.product = product;
        lbAuctionName.setText(product.getName());
        lbGiaSp.setText(product.getDescription());
        if (product.getImagePath() != null){
            Image image = new Image(getClass().getResourceAsStream(product.getItemId()));  // Tao tam anh tu duong dan
            imgSanPham.setImage(image); // dan tam anh vao khung
        }
    }

    @FXML
    public void btnLiveAuction(ActionEvent event){
        try {

            // Nap man hinh giao dien man_hinh_sp
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/man_hinh_hien_thi_sp.fxml"));
            Parent root = loader.load();  // tim file FXML doc ban ve va tao giao dien xac ( chua co bo nao)

            ManHinhHienThiSpController controller = loader.getController(); // truyen vao bo nao cua giao dien
            controller.setProductData( product);  // Truyen du lieu vao ManHinhHienThiSp

            // Truy nguoc lai cua so de ra lenh cho giao dien phai lm j do
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);  //Chuyen man = setRoot

        }catch(IOException e){
            e.printStackTrace();

        }

    }

}
