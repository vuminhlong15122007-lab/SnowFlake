package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class UserProfileController implements ResponseListener {
    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);
    @FXML private TextField updateNameText;
    @FXML private TextField updateEmailText;
    @FXML private TextField updatePhoneText;
    @FXML private ImageView myImageView;
    @FXML
    public void initialize() {
        loadUserInfo();
    }


    @FXML
    public void clickToBackToSceneMain(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
        }

    @FXML
    public void clickToLogOut(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
        }

    @FXML
    public void clickToSellerMangement(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductManagement.fxml");
    }
    @FXML
    public void clickToAuctionList(ActionEvent event){
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
    }

    @FXML
    public void goParticipatedAuction(ActionEvent event ){
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/UserParticipatedAuction.fxml");
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
        ImageHelper.loadBase64ToImageView( myImageView, base64Data);
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
            showAlert("Loi", "Vui long nhap day du ten, email va so dien thoai." , "Wait.gif");
            return;
        }
        new Thread(() -> {
            try {
                UpdateProfileCommand cmd = new UpdateProfileCommand();
                cmd.addData("userId", currentUser.getId());
                cmd.addData("username", newName);
                cmd.addData("email", newEmail);
                cmd.addData("phone", newPhone);
                cmd.addData("avt", currentUser.getImagePath());

                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.sendRequest(cmd, this); } catch (ConnectionFailedException e) {
                log.error("Lỗi kết nối: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối đến server"));
            } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu cập nhật"));
            } catch (Exception e) {
                log.error("Lỗi cập nhật: {}", e.getMessage(), e);
                Platform.runLater(() -> showAlert("Lỗi", "Cập nhật thất bại: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void clickToResetPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/reset_password.fxml"));
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            log.error("Lỗi mở popup reset password: {}", e.getMessage(), e);
            showAlert("Lỗi", "Không thể mở cửa sổ đổi mật khẩu");
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
        try{
        byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
        String base64Data = ImageHelper.fileToBase64(fileContent);
        ImageHelper.loadBase64ToImageView( myImageView, base64Data);
            User user = ClientModel.getInstance().getCurrentUser();
            if (user != null) {
                user.setImagePath(base64Data);
            }
        }catch (Exception e) {
            log.error("Lỗi chọn ảnh: {}", e.getMessage(), e);
            showAlert("Lỗi", "Không thể tải ảnh lên");
        }
    }
    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public void onResponse(Response response) {
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(UpdateProfileCommand.class, this);
        Platform.runLater(() -> {
            if (response != null && response.isSuccess() && response.getPayLoad() instanceof User updatedUser) {
                ClientModel.getInstance().setCurrentUser(updatedUser);
                loadUserInfo();
                showAlert("Thành công", "Cập nhật thông tin thành công", "Happy.gif");
                return;
            }

            String message = response == null ? "Khong nhan duoc phan hoi tu server." : response.getMessage();
            showAlert("That bai", message, "False.gif");
        });
    }
}
