package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.MainApplication;
import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAccountCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.scene.control.CheckBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class RegisterController implements ResponseListener {
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    @FXML public TextField PhoneNumber;
    @FXML private TextField Username;
    @FXML private TextField Email;
    @FXML private PasswordField Password;
    @FXML private PasswordField Confirm_Password;
    @FXML private Label message;
    @FXML private TextField PasswordVisible;
    @FXML private TextField ConfirmPasswordVisible;
    @FXML private CheckBox agreeCheckBox;

    private boolean passwordShown = false;
    private boolean confirmPasswordShown = false;
    ActionEvent signUpEvent;

    @FXML
    public void clickBackToLogin(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }
    public void clickSignUp(ActionEvent event) {
        signUpEvent = event;
        String name = Username.getText();
        String password;
        if (passwordShown){
            password = PasswordVisible.getText();
        } else {
            password = Password.getText();
        }
         Password.getText();
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

        if (!agreeCheckBox.isSelected()){
            message.setText("Vui lòng đồng ý với điều khoản");
            return;
        }

        String finalPassword = password;
        new Thread(() -> {
            try {
                ServerConnection connection = NetworkManager.getConnection();
                Command cmd = new AddAccountCommand();
                cmd.addData("username", name);
                cmd.addData("password", finalPassword);
                cmd.addData("email", email);
                cmd.addData("sdt", sdt);
                cmd.addData("accountType", "USER");
                connection.sendCommand(cmd);
                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.register(AddAccountCommand.class, this);
            } catch (ConnectionFailedException e) {
                log.error("Lỗi kết nối: {}", e.getMessage());
                Platform.runLater(() -> message.setText("Lỗi kết nối server"));
            } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(() -> message.setText("Không thể gửi yêu cầu đăng ký"));
            } catch (Exception e) {
                log.error("Lỗi đăng ký: {}", e.getMessage(), e);
                Platform.runLater(() -> message.setText("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    // ẩn hiện mkh
    @FXML
    public void togglePasswordVisibility() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            PasswordVisible.setText(Password.getText());
            PasswordVisible.setVisible(true);
            Password.setVisible(false);
        } else {
            Password.setText(PasswordVisible.getText());
            Password.setVisible(true);
            PasswordVisible.setVisible(false);
        }
    }
    //ẩn hiện xác nhận mk
    @FXML
    public void toggleConfirmPasswordVisibility() {
        confirmPasswordShown = !confirmPasswordShown;
        if (confirmPasswordShown) {
            ConfirmPasswordVisible.setText(Confirm_Password.getText());
            ConfirmPasswordVisible.setVisible(true);
            Confirm_Password.setVisible(false);
        } else {
            Confirm_Password.setText(ConfirmPasswordVisible.getText());
            Confirm_Password.setVisible(true);
            ConfirmPasswordVisible.setVisible(false);
        }
    }

    // Mở popup đkhoan
    @FXML
    public void showTerms() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(
                    "/com/javfxtutorial/hethongdaugia/view/fxml/TermsPopup.fxml"));
            Scene scene = new Scene(loader.load());
            ThemeManager.apply(scene);


            TermsController termsController = loader.getController();

            Stage termsStage = new Stage();
            termsStage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            termsStage.setTitle("Điều khoản sử dụng SnowFox");
            termsStage.initModality(Modality.APPLICATION_MODAL); //không thể bấm vào cửa sổ đăng ký phía sau cho đến khi đóng popup lại
            termsStage.setScene(scene);

            // nhận kq đý / Từ chối
            termsController.setResultCallback(agreed -> agreeCheckBox.setSelected(agreed));

            termsStage.show();
        } catch (IOException e) {
            log.error("Lỗi mở popup terms: {}", e.getMessage(), e);
            showAlert("Lỗi", "Không thể mở điều khoản");
        }
    }

    @Override
    public void onResponse(Response rp) {
        Platform.runLater(() -> {
        if (rp.isSuccess()){
            Stage stage = new Stage();
            stage.setTitle("Tạo Tài Khoản Thành Công");
            try{
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/SignUpSuccessPopup.fxml"));
                stage.initStyle(StageStyle.TRANSPARENT);
                Scene scene = null;
                scene = new Scene(fxmlLoader.load());
                ThemeManager.apply(scene);
                scene.setFill(Color.TRANSPARENT);
                stage.setScene(scene);
                stage.show();
                changeScene(signUpEvent,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");} catch (IOException e) {
                log.error("Lỗi load popup thành công: {}", e.getMessage(), e);
                showAlert("Thành công", "Đăng ký thành công! Vui lòng đăng nhập.");
                changeScene(signUpEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
            }

        }else{
            message.setText(rp.getMessage());
        }
        });
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(AddAccountCommand.class, this);
    }


}

