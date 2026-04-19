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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class RegisterController {
    @FXML public TextField PhoneNumber;
    @FXML private TextField Username;
    @FXML private TextField Email;
    @FXML private PasswordField Password;
    @FXML private PasswordField Confirm_Password;
    @FXML private Label message;

    @FXML
    public void clickBackToLogin(ActionEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void clickSignUp(ActionEvent event) throws IOException, ClassNotFoundException {
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
                ServerConnection connection = new ServerConnection();
                Command cmd = new RegisterCommand();
                cmd.addData("username", name);
                cmd.addData("password", password);
                cmd.addData("email", email);
                cmd.addData("sdt", sdt);
                connection.sendCommand(cmd);
                Response rp = connection.receiveResponse();
                if (rp.isSuccess()){

                    Stage stage = new Stage();
                    stage.setTitle("Tạo Tài Khoản Thành Công");
                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/popUpSignUp.fxml"));
                    stage.initStyle(StageStyle.DECORATED);
                    Scene scene = new Scene(fxmlLoader.load());
                    stage.setScene(scene);
                    stage.show();
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
                        Stage stage1 = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage1.setScene(new Scene(root));
                        stage1.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    showAlert("Đăng ký không thành công", rp.getMessage());
                }
                connection.close();

            }
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}

