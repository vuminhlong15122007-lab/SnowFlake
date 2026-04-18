package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.MainApplication;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAccountCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.RegisterCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Edit_User_Popup_Controller {
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhoneNumber;
    @FXML private ComboBox<String> cbRole ;
    @FXML private ComboBox cbStatus ;
    @FXML private Button btnCancel;
    @FXML private Label message;

    @FXML
    public void initialize() {
        // gan su kien dong cua so cho nut huy
        btnCancel.setOnAction(event -> {
            // Llay va dong stage hien tai
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        });
        //them chon vai tro
        cbRole.setItems(FXCollections.observableArrayList(
                "USER",
                "ADMIN"));
    }

    @FXML
    public void clickToSave(ActionEvent event) throws IOException, ClassNotFoundException {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String sdt = txtPhoneNumber.getText();
        String selectRole = cbRole.getValue();
        String password = "000000";
        String confirmPassword = "000000";
        //khong de o trong
        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || sdt.isEmpty() || selectRole == null){
            message.setText("Vui lòng điền đầy đủ thông tin!");
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
        if (!name.isEmpty() && !email.isEmpty() && selectRole != null){
            ServerConnection connection = new ServerConnection();
            Command cmd = new AddAccountCommand();
            cmd.addData("username", name);
            cmd.addData("password", password);
            cmd.addData("email", email);
            cmd.addData("sdt", sdt);
            cmd.addData("accountType", selectRole);
            connection.sendCommand(cmd);
            Response rp = connection.receiveResponse();
            if (rp.isSuccess()) {
                Stage stage1 = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage1.close();
                Stage stage = new Stage();
                stage.setTitle("Tạo Tài Khoản Thành Công");
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/popUpSignUp.fxml"));
                stage.initStyle(StageStyle.DECORATED);
                Scene scene = new Scene(fxmlLoader.load());
                stage.setScene(scene);
                stage.show();
            }else{
                showAlert("Đăng ký không thành công", rp.getMessage());
            }
                connection.close();

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
