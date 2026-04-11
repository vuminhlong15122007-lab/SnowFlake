package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InvalidObjectException;


public class GoLiveActionController {
    @FXML Button goMenu;
    @FXML Button placeBidButton;
    @FXML Label giaHienTai;
    @FXML Label ngMua; // chua xu ly duoc
    @FXML Label productName;
    @FXML ImageView productImage;

    private  Item product;

//    public void setDataSang(Item p){
//        this.product = p;
//        giaHienTai.setText(p.getGiaHienTai());
//        productName.setText(p.getName());
//        if (p.getImagePath()!= null){
//            String imag = "/com/javfxtutorial/hethongdaugia/assets/" + p.getImagePath();
//            Image image = new Image(getClass().getResourceAsStream(imag));
//            productImage.setImage(image);
//        }
//    }

    public void goMenu(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        }catch (IOException e){
            e.printStackTrace();
        }



    }
}
