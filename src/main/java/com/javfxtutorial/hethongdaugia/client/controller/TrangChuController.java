package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class TrangChuController {
    //truy cap thong tin nguoi dung
    @FXML private Button profileButton;
    @FXML private Button liveAuctionButton; //btn dan den phien dau gia

    @FXML
    public void goToProfile(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/man_hinh_hien_thong_tin_User.fxml"));
            Parent root = loader.load();
            Stage stage =  (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void goAuction(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/auction_list.fxml"));
            Parent root = loader.load();
            Stage stage =  (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}
