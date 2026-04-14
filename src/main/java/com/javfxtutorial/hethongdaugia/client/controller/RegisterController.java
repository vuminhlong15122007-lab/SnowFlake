package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.MainApplication;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.RegisterCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class RegisterController {
    @FXML
    public TextField PhoneNumber;
    @FXML
    private TextField Username;
    @FXML
    private TextField Email;
    @FXML
    private PasswordField Password;
    @FXML
    private PasswordField Confirm_Password;

    @FXML
    public void clickBackToLogin(ActionEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void clickSignUp(ActionEvent event) throws IOException {
        String name = Username.getText();
        String password = Password.getText();
        String email = Email.getText();
        String sdt = PhoneNumber.getText();
        String confirmPassword = Confirm_Password.getText();
        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()){
            if (password.equals(confirmPassword)){
                ServerConnection connection = new ServerConnection();
                Command cmd = new RegisterCommand();
                cmd.addData("username", name);
                cmd.addData("password", password);
                cmd.addData("email", email);
                cmd.addData("sdt", sdt);
                Response rp = connection.sendCommand(cmd);
                if (rp.isSuccess()){
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Stage stage = new Stage();
                    stage.setTitle("Tạo Tài Khoản Thành Công");
                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/popUpSignUp.fxml"));
                    stage.initStyle(StageStyle.DECORATED);
                    Scene scene = new Scene(fxmlLoader.load());
                    stage.setScene(scene);
                    stage.show();
                }
                connection.close();

            }
        }
    }

}

