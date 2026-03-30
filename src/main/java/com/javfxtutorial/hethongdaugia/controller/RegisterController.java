package com.javfxtutorial.hethongdaugia.controller;

import com.javfxtutorial.hethongdaugia.MainApplication;
import com.javfxtutorial.hethongdaugia.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.model.User;
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
import javafx.stage.StageStyle;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField Username;
    @FXML
    private TextField Email;
    @FXML
    private PasswordField Password;
    @FXML
    private PasswordField Confirm_Password;

    @FXML
    private Button backtologin;
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
        String email = Email.getText();
        String password = Password.getText();
        String sdt = Confirm_Password.getText();
        String confirmPassword = Confirm_Password.getText();
        if (!name.isEmpty() || !email.isEmpty() || !password.isEmpty() || !confirmPassword.isEmpty()){
            if (password.equals(confirmPassword)){
                UserDAO.getInstance().insert(new User(name,password,email, sdt));
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stage stage = new Stage();
                stage.setTitle("Ngu như bò");
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/popUpSignUp.fxml"));
                stage.initStyle(StageStyle.DECORATED);
                Scene scene = new Scene(fxmlLoader.load());
                stage.setScene(scene);
                stage.show();

            }
        }

    }

}

