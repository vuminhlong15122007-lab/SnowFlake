package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.MainApplication;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAccountCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class Edit_User_Popup_Controller implements ResponseListener {
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
    ActionEvent saveEvent;
    @FXML
    public void clickToSave(ActionEvent event) throws IOException, ClassNotFoundException {
        saveEvent = event;
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
            ServerConnection connection = NetworkManager.getConnection();
            Command cmd = new AddAccountCommand();
            cmd.addData("username", name);
            cmd.addData("password", password);
            cmd.addData("email", email);
            cmd.addData("sdt", sdt);
            cmd.addData("accountType", selectRole);
            connection.sendCommand(cmd);
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.register(AddAccountCommand.class, this);
        }
    }

    @Override
    public void onResponse(Response rp) {
        Platform.runLater(() -> {
        if (rp.isSuccess()) {
            Stage stage1 = (Stage) ((Node) saveEvent.getSource()).getScene().getWindow();
            stage1.close();
            Stage stage = new Stage();
            stage.setTitle("Tạo Tài Khoản Thành Công");
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/SignUpSuccessPopup.fxml"));
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        }else{
            showAlert("Đăng ký không thành công", rp.getMessage() , "False.gif");
        }
        });
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(AddAccountCommand.class, this);
    }
}
