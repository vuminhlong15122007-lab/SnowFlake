package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.*;

import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteUserCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllUsersCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminUserController implements Initializable, ResponseListener {
    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TextField searchField;
    private User selectUser;

    private ObservableList<User> danhSach;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        loadUserData();
    }

    @FXML
    private void loadUserData() {
        new Thread(
                        () -> {
                            try {
                                Command cmd = new GetAllUsersCommand();
                                NetworkManager networkManager = NetworkManager.getInstance();
                                networkManager.sendRequest(cmd, this);
                            } catch (ConnectionFailedException e) {
                                log.error("Lỗi kết nối: {}", e.getMessage());
                                Platform.runLater(
                                        () ->
                                                showAlert(
                                                        "Lỗi kết nối",
                                                        "Không thể kết nối đến server"));
                            } catch (SendFailedException e) {
                                log.error("Lỗi gửi: {}", e.getMessage());
                                Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu"));
                            } catch (Exception e) {
                                log.error("Lỗi tải user: {}", e.getMessage(), e);
                                Platform.runLater(() -> showAlert("Lỗi", "Tải dữ liệu thất bại"));
                            }
                        })
                .start();
    }

    @FXML
    public void getUpdateAdmin(ActionEvent event) {
        changePopup(
                event,
                "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_UpdateAdminInfo.fxml",
                "sửa thông tin admin");
    }

    public void clickToAddUser(ActionEvent event) {
        changePopup(
                event,
                "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_UpdateUserInfo.fxml",
                "Thêm User mới");
    }

    @FXML
    public void clickToDeleteUser() throws ConnectionFailedException, SendFailedException {
        selectUser = userTable.getSelectionModel().getSelectedItem();
        if (selectUser == null) {
            showAlert("Lỗi", "Vui lòng chọn người dùng cần xóa", "WrongCat.gif");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn chắc chắn muốn xóa tài khoản?");
        confirmAlert.setContentText(
                "Tài khoản: "
                        + selectUser.getName()
                        + "\nEmail: "
                        + selectUser.getEmail()
                        + "\nSố điện thoại: "
                        + selectUser.getSdt());
        ButtonType yes = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yes, no);
        ButtonType result = confirmAlert.showAndWait().orElse(null);
        // neu co
        if (result == yes) {
            // tao command gui len server
            DeleteUserCommand cmd = new DeleteUserCommand();
            cmd.addData("userId", selectUser.getId());
            cmd.addData("username", selectUser.getName());
            cmd.addData("email", selectUser.getEmail());
            cmd.addData("phone", selectUser.getSdt());

            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.sendRequest(cmd, this);
        }
    }

    @FXML
    public void clickToResetPW() {
        try {
            Parent root =
                    FXMLLoader.load(
                            Objects.requireNonNull(
                                    getClass()
                                            .getResource(
                                                    "/com/javfxtutorial/hethongdaugia/view/fxml/reset_password.fxml")));

            Stage stage = new Stage();
            // khong dong cua so cu ma khoa cua so cu o phía  sau
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reLoad() {
        loadUserData(); // ✅ Gọi lại method loadUserData() thay vì gọi DAO
        System.out.println("Dữ liệu đã được cập nhật!");
    }

    @FXML
    public void logOut(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    @FXML
    public void clickToSearch() {
        String textWord = searchField.getText();

        if (textWord == null || textWord.trim().isEmpty()) {
            userTable.setItems(danhSach);
            return;
        }

        ObservableList<User> result = FXCollections.observableArrayList();
        String keyword = textWord.toLowerCase().trim();

        for (User user : danhSach) {
            if (String.valueOf(user.getId()).toLowerCase().contains(keyword)
                    || user.getEmail().toLowerCase().contains(keyword)
                    || user.getName().toLowerCase().contains(keyword)) {
                result.add(user);
            }
        }
        userTable.setItems(result);
        System.out.println("Đã tìm thấy " + result.size() + " kết quả");
    }

    public void clickToDeleteSearch() {
        searchField.clear();
        userTable.setItems(danhSach);
    }

    @FXML
    public void clickToGoItemAdmin(ActionEvent event) {
        changeScene(
                event, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_ProductManagement.fxml");
    }

    @Override
    public void onResponse(Response rp) {
        if (rp.getCommand().getClass() == DeleteUserCommand.class) {
            if (rp.isSuccess()) {
                showAlert("Xóa thành công", rp.getMessage(), "Kiss.gif");
                userTable.getItems().remove(selectUser);
            } else {
                showAlert("Lỗi", rp.getMessage(), "WrongCat.gif");
            }
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.unregister(DeleteUserCommand.class, this);
        }
        if (rp.getCommand().getClass() == GetAllUsersCommand.class) {
            if (rp.isSuccess()) {
                List<User> users = (List<User>) rp.getPayLoad();
                javafx.application.Platform.runLater(
                        () -> {
                            danhSach = FXCollections.observableArrayList(users);
                            userTable.setItems(danhSach);
                        });
            } else {
                javafx.application.Platform.runLater(() -> showAlert("Lỗi", rp.getMessage()));
            }
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.unregister(GetAllUsersCommand.class, this);
        }
    }
}
