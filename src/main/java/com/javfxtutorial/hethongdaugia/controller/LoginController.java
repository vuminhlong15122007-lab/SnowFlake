package com.javfxtutorial.hethongdaugia.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField Username ;
    @FXML private PasswordField Password ;
    @FXML private Button loginButton;
    @FXML private Button SignIn;
    @FXML
    public void login(ActionEvent event) {
        String username = Username.getText();
        String password = Password.getText();
    }

    public void sign_in(ActionEvent envent){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Sign_In.fxml"));
            Stage stage = (Stage) SignIn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}