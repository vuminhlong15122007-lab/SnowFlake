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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class UserProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private TextField updateNameText;
    @FXML private TextField updateEmailText;
    @FXML private TextField updatePhoneText;
    @FXML private ImageView myImageView;
    @FXML private Button resetPW;

    @FXML
    public void initialize() {
        loadUserInfo();
    }

    @FXML
    public void displayImage(Image img) {
        if (img != null && myImageView != null) {
            myImageView.setImage(img);
        }
    }

    @FXML
    public void clickToBackToSceneMain(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");
    }

    @FXML
    public void clickToLogOut(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    @FXML
    public void clickToSellerMangement(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/quan_ly_san_pham_seller.fxml");
    }

    @FXML
    public void loadUserInfo() {
        User currentUser = ClientModel.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        updateNameText.setText(currentUser.getName());
        updateEmailText.setText(currentUser.getEmail());
        updatePhoneText.setText(currentUser.getSdt());

        String base64Data = currentUser.getImagePath();
        if (base64Data == null || base64Data.isBlank()) {
            if (myImageView != null) {
                myImageView.setImage(null);
            }
            return;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            if (myImageView != null) {
                myImageView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Loi load anh: " + e.getMessage());
            if (myImageView != null) {
                myImageView.setImage(null);
            }
        }
    }

    @FXML
    public void handleUpdateInfo() {
        User currentUser = ClientModel.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Loi", "Chua dang nhap");
            return;
        }

        String newName = safeTrim(updateNameText.getText());
        String newEmail = safeTrim(updateEmailText.getText());
        String newPhone = safeTrim(updatePhoneText.getText());
        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
            showAlert("Loi", "Vui long nhap day du ten, email va so dien thoai.");
            return;
        }

        ServerConnection connection = new ServerConnection();
        try {
            if (connection.getOut() == null || connection.getIn() == null) {
                showAlert("Loi", "Khong the ket noi toi server.");
                return;
            }

            UpdateProfileCommand cmd = new UpdateProfileCommand();
            cmd.addData("userId", currentUser.getId());
            cmd.addData("username", newName);
            cmd.addData("email", newEmail);
            cmd.addData("phone", newPhone);
            cmd.addData("avt", currentUser.getImagePath());

            connection.sendCommand(cmd);
            Response response = connection.receiveResponse();
            if (response != null && response.isSuccess() && response.getPayLoad() instanceof User) {
                User updatedUser = (User) response.getPayLoad();
                ClientModel.getInstance().setCurrentUser(updatedUser);
                loadUserInfo();
                showAlert("Thanh cong", "Cap nhat thong tin thanh cong");
                return;
            }

            String message = response == null ? "Khong nhan duoc phan hoi tu server." : response.getMessage();
            showAlert("That bai", message);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert("Loi", "Khong the cap nhat thong tin. Kiem tra log server de biet chi tiet.");
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                System.err.println("Loi dong ket noi: " + e.getMessage());
            }
        }
    }

    @FXML
    public void clickToResetPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/reset_password.fxml"));
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clickToChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chon anh dai dien");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        try {
            byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
            String base64String = Base64.getEncoder().encodeToString(fileContent);
            User user = ClientModel.getInstance().getCurrentUser();
            if (user != null) {
                user.setImagePath(base64String);
            }
            if (myImageView != null) {
                myImageView.setImage(new Image(new ByteArrayInputStream(fileContent)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Loi", "Khong the tai anh len!");
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
