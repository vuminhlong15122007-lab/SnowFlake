package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.MainApplication;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.RegisterCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class RegisterController implements ResponseListener {
    @FXML public TextField PhoneNumber;
    @FXML private TextField Username;
    @FXML private TextField Email;
    @FXML private PasswordField Password;
    @FXML private PasswordField Confirm_Password;
    @FXML private Label message;
    ActionEvent signUpEvent;
    @FXML
    public void clickBackToLogin(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }
    public void clickSignUp(ActionEvent event) throws IOException, ClassNotFoundException {
        signUpEvent = event;
        String name = Username.getText();
        String password = Password.getText();
        String email = Email.getText();
        String sdt = PhoneNumber.getText();
        String confirmPassword = Confirm_Password.getText();
        //khong de o trong
        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || sdt.isEmpty()){
            message.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        //check pasword du 6 ki tu
        if(password.length() < 6){
            message.setText("Mật khẩu phải đủ tối thiểu 6 kí tự!");
            return;
        }
        //check confirm password
        if (!password.equals(confirmPassword)){
            message.setText("Vui lòng xác thực khớp mật khẩu!");
            return;
        }
        //check so dien thoai
        if(sdt.length() != 10){
            message.setText("Số điện thoại phải đủ 10 số!");
            return;
        }
        try{
            Long.parseLong(sdt);
        } catch (NumberFormatException e) {
            message.setText("Số điện thoại chỉ bao gồm các số!");
            return;
        }
        //check email
        if (!email.endsWith("@gmail.com")) {
            message.setText(" Email phải có đuôi @gmail.com!");
            return;
        }


        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()){
            if (password.equals(confirmPassword)){
                ServerConnection connection = NetworkManager.getConnection();
                Command cmd = new RegisterCommand();
                cmd.addData("username", name);
                cmd.addData("password", password);
                cmd.addData("email", email);
                cmd.addData("sdt", sdt);
                connection.sendCommand(cmd);
                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.register(RegisterCommand.class, this);

            }
        }
    }

    @Override
    public void onResponse(Response rp) {
        Platform.runLater(() -> {
        if (rp.isSuccess()){
            Stage stage = new Stage();
            stage.setTitle("Tạo Tài Khoản Thành Công");
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/SignUpSuccessPopup.fxml"));
            stage.initStyle(StageStyle.DECORATED);
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            stage.setScene(scene);
            stage.show();
            changeScene(signUpEvent,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");

        }else{
            showAlert("Đăng ký không thành công", rp.getMessage() , "False.gif");
        }
        });
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(RegisterCommand.class, this);
    }
}

