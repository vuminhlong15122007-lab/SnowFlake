package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class Update_Admin_Controller {
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private Button btnCancel;
    //lay du lieu tu login de hien thi
    //ham tu dong chay khi load man hinh
    @FXML
    public void initialize(){
        // gan su kien dong cua so cho nut huy
        btnCancel.setOnAction(event -> {
            // Llay va dong stage hien tai
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        });
        loadUserInfo();
    }
    //lay du lieu tu clientmodel de hien thi
    @FXML
    public void loadUserInfo(){
        User currentUser = ClientModel.getInstance().getCurrentUser();
        if (currentUser != null){
            txtName.setText(currentUser.getName());
            txtEmail.setText(currentUser.getEmail());
            txtPhone.setText(currentUser.getSdt());
        }else{
            txtName.setText("");
            txtEmail.setText("");
            txtPhone.setText("");
        }
    }
    //cap nhat thong tin
    @FXML
    public void handleUpdateInfo() throws IOException, ClassNotFoundException {
        //lay du lieu tu o nhap
        String newName = txtName.getText();
        String newEmail = txtEmail.getText();
        String newPhone = txtPhone.getText();

        //lay user hien tai

        User currentUser = ClientModel.getInstance().getCurrentUser();
        if(currentUser == null){
            showAlert("Lỗi", "Chưa đăng nhập");
            return;
        }

        //tao command gui len server
        ServerConnection connection = new ServerConnection();
        UpdateProfileCommand cmd = new UpdateProfileCommand();
        cmd.addData("userId", currentUser.getId());
        cmd.addData("username", newName);
        cmd.addData("email", newEmail);
        cmd.addData("phone", newPhone);

        connection.sendCommand(cmd);
        Response rp = connection.receiveResponse();

        if(rp.isSuccess()){
            //cap nhat lai clientmodel voi user moi
            User updateUser = (User) rp.getPayLoad();
            ClientModel.getInstance().setCurrentUser(updateUser);

            //load lai man hinh
            loadUserInfo();
            showAlert("Thành công", "Cập nhật thông tin thành công");
        }else{
            showAlert("Thất bại", rp.getMessage());
        }
        connection.close();
    }

    //hien thi alert
    public void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
