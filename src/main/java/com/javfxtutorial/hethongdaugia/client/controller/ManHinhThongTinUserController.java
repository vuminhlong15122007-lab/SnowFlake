package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManHinhThongTinUserController {
    // Lay du lieu tu Logic dang nhap ( Data)
    @FXML
    public void logOut1(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void logOut(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
