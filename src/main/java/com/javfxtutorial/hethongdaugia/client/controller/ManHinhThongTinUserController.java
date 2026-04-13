package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class ManHinhThongTinUserController {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;

    @FXML private TextField updateNameText;
    @FXML private TextField updateEmailText;
    @FXML private TextField updatePhoneText;
    // Lay du lieu tu Logic dang nhap ( Data)
    @FXML
    public void logOut1(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void logOut(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //lay du lieu tu login de hien thi
    //ham tu dong chay khi load man hinh
    @FXML
    public void initialize(){
        loadUserInfo();
    }
    //lay du lieu tu clientmodel de hien thi
    @FXML
    public void loadUserInfo(){
        User currentUser = ClientModel.getInstance().getCurrentUser();
        if (currentUser != null){
            updateNameText.setText(currentUser.getName());
            updateEmailText.setText(currentUser.getEmail());
            updatePhoneText.setText(currentUser.getSdt());
        }else{
            updateNameText.setText("");
            updateEmailText.setText("");
            updatePhoneText.setText("");
        }
    }
    //cap nhat thong tin
    @FXML
    public void handleUpdateInfo(){
        //lay du lieu tu o nhap
        String newName = updateNameText.getText();
        String newEmail = updateEmailText.getText();
        String newPhone = updatePhoneText.getText();

        //lay user hien tai

        User currentUser = ClientModel.getInstance().getCurrentUser();
        if(currentUser == null){
            showAlert("Lỗi", "Chưa đăng nhập");
            return;
        }

        //tao command gui len server
        ServerConnection connection = new ServerConnection(5000);
        UpdateProfileCommand cmd = new UpdateProfileCommand();
        cmd.addData("userId", currentUser.getId());
        cmd.addData("username", newName);
        cmd.addData("email", newEmail);
        cmd.addData("phone", newPhone);

        Response rp = connection.sendCommand(cmd);

        if(rp.isSuccess()){
            //cap nhat lai clientmodel voi user moi
            User updateUser = (User) rp.getPayLoad();
            ClientModel.getInstance().setCurrentUser(updateUser);

            //load lai man hinh
            loadUserInfo();
            showAlert("Thành công", "Cập nhật thông tin thành công");
        }else{
            showAlert("Thất bại", rp.getMessage());
        }}

    //hien thi alert
    public void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
