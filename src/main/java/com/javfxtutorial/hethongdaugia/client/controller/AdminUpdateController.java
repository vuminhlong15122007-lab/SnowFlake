package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class AdminUpdateController implements ResponseListener {
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
    public void handleUpdateInfo() throws IOException, ClassNotFoundException, ConnectionFailedException, SendFailedException {
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
        ServerConnection connection = NetworkManager.getConnection();
        UpdateProfileCommand cmd = new UpdateProfileCommand();
        cmd.addData("userId", currentUser.getId());
        cmd.addData("username", newName);
        cmd.addData("email", newEmail);
        cmd.addData("phone", newPhone);

        connection.sendCommand(cmd);
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.register(UpdateProfileCommand.class, this);

    }

    @Override
    public void onResponse(Response rp) {
        if(rp.isSuccess()){
            //cap nhat lai clientmodel voi user moi
            User updateUser = (User) rp.getPayLoad();
            ClientModel.getInstance().setCurrentUser(updateUser);

            //load lai man hinh
            loadUserInfo();
            showAlert("Thành công", "Cập nhật thông tin thành công" , "FunnyCat.gif");
        }else{
            showAlert("Thất bại", rp.getMessage() , "WrongCat.gif");
        }
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(GetAllAuctionsCommand.class, this);
    }
}
