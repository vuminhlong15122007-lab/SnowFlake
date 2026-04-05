package com.javfxtutorial.hethongdaugia.client.controller;

import com.sun.source.tree.TryTree;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.awt.event.ActionEvent;
import java.io.IOException;


public class ItemphiendaugiaController {
    @FXML Label lbAuctionName;
    @FXML Label lbGiaSp;
    @FXML Button btnDauGia;
    @FXML Label lbLuotDau;  // 2 CAI Label cuoi chx xu ly duoc
    @FXML Label lbTimer;

    private com.javfxtutorial.hethongdaugia.common.model.Item product;

    public void setData(com.javfxtutorial.hethongdaugia.common.model.Item product){   // xu ly du lieu tu Obj den giao dien
        this.product = product;
        lbAuctionName.setText(product.getName());
        lbGiaSp.setText(product.getDescription());
    }

    @FXML
    public void btnLiveAuction(ActionEvent event){
        try {

            // Nap man hinh de sau khi nhan nut muon hien ra man_hinh_sp
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/man_hinh_hien_thi_sp.fxml"));

            // Truy nguoc lai cua so de ra lenh cho giao dien phai lm j do
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            //Truyen du lieu da nap vao
            stage.setScene(new Scene(root));
            stage.show();

        }catch(IOException e){


        }

    }

}
