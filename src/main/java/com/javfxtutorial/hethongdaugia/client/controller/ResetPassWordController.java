package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ResetPassWordController {
    @FXML private TextField txtoldPW;
    @FXML private TextField txtNewPW;
    @FXML private TextField txtConfirmPW;
    @FXML
    public void initialize(){
        loadUserInfo();
    }
    //lay du lieu tu clientmodel de hien thi
    @FXML
    public void loadUserInfo(){
        User currentUser = ClientModel.getInstance().getCurrentUser();
        if (currentUser != null){
            txtoldPW.setText(currentUser.getPassWord());
            txtNewPW.setText("");
            txtConfirmPW.setText("");
        }else{
            txtoldPW.setText("");
            txtNewPW.setText("");
            txtConfirmPW.setText("");
        }
    }
    @FXML
    public void updatePW() throws IOException {
        //lay du lieu tu o nhap
        String newPW = txtNewPW.getText();
        String confirmPW = txtConfirmPW.getText();

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
        cmd.addData("passWord", txtNewPW);

        Response rp = connection.sendCommand(cmd);

        if(rp.isSuccess()){
            //cap nhat lai clientmodel voi user moi
            User updateUser = (User) rp.getPayLoad();
            ClientModel.getInstance().setCurrentUser(updateUser);

            //load lai man hinh
            loadUserInfo();
            showAlert("Thành công", "Cập nhật mật khẩu thành công");
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
