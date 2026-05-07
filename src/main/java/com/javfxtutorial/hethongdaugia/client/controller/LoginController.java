package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.LoginCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

public class LoginController implements ResponseListener, Initializable {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @FXML private TextField Username ;
    @FXML private PasswordField Password ;
    @FXML private Label message;
    ActionEvent loginEvent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ServerConnection connection = ServerConnection.getInstance();
    }

    @FXML
    public void clickLogin(ActionEvent event) throws IOException, ClassNotFoundException {
        loginEvent = event;
        String username = Username.getText();
        String password = Password.getText();
        new Thread(() -> {
            ServerConnection connection = ServerConnection.getInstance();
            Command cmd = new LoginCommand();
            cmd.addData("username", username);
            cmd.addData("password", password);
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.register(cmd.getClass(), this );
            connection.sendCommand(cmd);
        }).start();
    }

    public void clickCreateAccount(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/SignUp.fxml");
    }


    @Override
    public void onResponse(Response rp) {
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(rp.getCommand().getClass(), this);
        if (rp.isSuccess()){
            User user = (User) rp.getPayLoad();
            ClientModel.getInstance().setCurrentUser(user);
            Platform.runLater(() -> {
                if (user.getAccountType() == AccountType.USER) {
                    log.info(rp.getMessage());
                    changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");
                } else if (user.getAccountType() == AccountType.ADMIN) {
                    log.info(rp.getMessage());
                    changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/Quan_Ly_User_Admin.fxml");
                }
            });
        }else {
            Platform.runLater(() -> {
                message.setText("Sai tên hoặc mật khẩu!");
                log.info(rp.getMessage());
            });
        }
    }


}