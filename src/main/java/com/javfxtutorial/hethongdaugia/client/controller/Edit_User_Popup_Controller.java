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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Edit_User_Popup_Controller {
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhoneNumber;
    @FXML
    private ComboBox<String> cbRole ;
    @FXML
    private ComboBox cbStatus ;
    @FXML private Button btnCancel;
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
    public void clickToSave(ActionEvent event) throws IOException {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String sdt = txtPhoneNumber.getText();
        String selectRole = cbRole.getValue();
        String password = "00000";
        String confirmPassword = "00000";
        if (!name.isEmpty() && !email.isEmpty() && !selectRole.isEmpty()){
            ServerConnection connection = new ServerConnection();
            Command cmd = new AddAccountCommand();
            cmd.addData("username", name);
            cmd.addData("password", password);
            cmd.addData("email", email);
            cmd.addData("sdt", sdt);
            cmd.addData("accountType", selectRole);
            Response rp = connection.sendCommand(cmd);
            if (rp.isSuccess()){
                    Stage stage1 = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage1.close();
                    Stage stage = new Stage();
                    stage.setTitle("Tạo Tài Khoản Thành Công");
                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/popUpSignUp.fxml"));
                    stage.initStyle(StageStyle.DECORATED);
                    Scene scene = new Scene(fxmlLoader.load());
                    stage.setScene(scene);
                    stage.show();

                connection.close();

            }
        }
    }
}
