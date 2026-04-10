package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class AdminUserManagementController {
    //cap nhat thong tin
    @FXML
    private Button btnEditUser;
    @FXML
    public void getUpdateUser(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/Popupmanhinhsuathongtinadmin.fxml"));
            Parent root = loader.load();
            //lay stage hien tai
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
