package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.LoginCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
    @FXML private Button login;
    @FXML private Button sign_in;

    @FXML
    public void clickLogin(ActionEvent event) throws IOException, ClassNotFoundException {
        String username = Username.getText();
        String password = Password.getText();
        ServerConnection connection = new ServerConnection();
        Command cmd = new LoginCommand();
        cmd.addData("username", username);
        cmd.addData("password", password);
        connection.sendCommand(cmd);
        Response rp = connection.receiveResponse();
        if (rp.isSuccess()){
            User user = (User) rp.getPayLoad();
            ClientModel.getInstance().setCurrentUser(user);
            if (user.getAccountType() == AccountType.USER){
                try {
                    System.out.println(rp.getMessage());
                    Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (user.getAccountType() == AccountType.ADMIN) {
                try {
                    System.out.println(rp.getMessage());
                    Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/Quan_Ly_User_Admin.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println(rp.getMessage());
        }
        connection.close();
    }

    public void clickCreateAccount(ActionEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/SignUp.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}