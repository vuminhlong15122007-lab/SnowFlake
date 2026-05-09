package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.ResetPassWordCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class PasswordResetController implements ResponseListener {
    @FXML private TextField txtNewPW;
    @FXML private TextField txtConfirmPW;
    @FXML private Button btnCancel;

    @FXML
    public void initialize(){
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
            txtNewPW.setText("");
            txtConfirmPW.setText("");
        }
    }

    User currentUser;
    String newPW;

    @FXML
    public void updatePW() throws IOException, ClassNotFoundException {
        //lay du lieu tu o nhap
        newPW = txtNewPW.getText();
        String confirmPW = txtConfirmPW.getText();

        //lay user hien tai

        currentUser = ClientModel.getInstance().getCurrentUser();
        if(currentUser == null){
            showAlert("Lỗi", "Chưa đăng nhập" , "Wait.gif");
            return;
        }

        //tao command gui len server
        ServerConnection connection = NetworkManager.getConnection();
        ResetPassWordCommand cmd = new ResetPassWordCommand();
        cmd.addData("userId", currentUser.getId());
        cmd.addData("passWord", newPW);

        connection.sendCommand(cmd);
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.register(ResetPassWordCommand.class, this);
    }

    @Override
    public void onResponse(Response rp) {
        if(rp.isSuccess()){
            currentUser.setPassWord(newPW);
            // System.out.println("PW từ server: " + newUser.getPassWord());

            //load lai man hinh
            loadUserInfo();
            showAlert("Thành công", "Cập nhật mật khẩu thành công" , "FunnyCat.gif");

        }else{
            showAlert("Thất bại", rp.getMessage() , "Wait.gif");
        }
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(ResetPassWordCommand.class, this);

    }
}
