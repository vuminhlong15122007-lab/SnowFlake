package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class QuanLySpSellerController {
    @FXML  TextField nameField;
    @FXML TextArea descriptionField;
    @FXML  TextField priceField;
    @FXML  TextField buocGia;

    public void goMenu(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
