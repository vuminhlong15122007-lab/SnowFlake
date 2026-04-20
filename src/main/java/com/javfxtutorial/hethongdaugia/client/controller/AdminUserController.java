package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteUserCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllUsersCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class AdminUserController implements Initializable {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private Button deleteButton;
    @FXML private Button reload;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button dltSearch;
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
        new Thread(() -> {
            ServerConnection connection = null;
            try {
                connection = ServerConnection.getInstance();
                Command cmd = new GetAllUsersCommand();
                connection.sendCommand(cmd);
                Response resp = connection.receiveResponse();
                if (resp.isSuccess()) {
                    List<User> users = (List<User>) resp.getPayLoad();
                    javafx.application.Platform.runLater(() -> {
                        danhSach = FXCollections.observableArrayList(users);
                        userTable.setItems(danhSach);
                    });
                } else {
                    javafx.application.Platform.runLater(() ->
                            showAlert("Lỗi", resp.getMessage())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        showAlert("Lỗi", "Không thể kết nối Server")
                );
            } finally {
                if (connection != null) {
                    try { connection.close(); } catch (IOException ex) {}
                }
            }
        }).start();
    }
    @FXML
    private Button btnEditUser;
    @FXML
    public void getUpdateAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/update_admin.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("sửa thông tin admin");
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void clickButtonExit(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");
    }
    public void clickToAddUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/Popupmanhinhsuathongtinadmin.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("Thêm User mới");
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private Button btnDeleteUser;
    @FXML
    public void clickToDeleteUser() throws IOException {
        ServerConnection connection =ServerConnection.getInstance();
        User selectUser = userTable.getSelectionModel().getSelectedItem();
        if (selectUser == null){
            showAlert("Lỗi", "Vui lòng chọn người dùng cần xóa");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn chắc chắn muốn xóa tài khoản?");
        confirmAlert.setContentText("Tài khoản: " + selectUser.getName() +
                "\nEmail: " + selectUser.getEmail() +
                "\nSố điện thoại: " + selectUser.getSdt());
        ButtonType yes = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yes, no);
        ButtonType result = confirmAlert.showAndWait().orElse(null);
        //neu co
        if(result == yes){
            //tao command gui len server
            DeleteUserCommand cmd = new DeleteUserCommand();
            cmd.addData("userId", selectUser.getId());
            cmd.addData("username", selectUser.getName());
            cmd.addData("email", selectUser.getEmail());
            cmd.addData("phone", selectUser.getSdt());

        try {
            connection.sendCommand(cmd);
            Response rp = connection.receiveResponse();
            if (rp.isSuccess()){
                showAlert("Xóa thành công", rp.getMessage());
                userTable.getItems().remove(selectUser);
            } else {
                showAlert("Lỗi", rp.getMessage());
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Có lỗi xảy ra khi kết nối với server: " + e.getMessage());
        }

    }
    connection.close();
}
    @FXML private Button btnResetPassword;
    @FXML
    public void clickToResetPW(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/reset_password.fxml"));

            Stage stage = new Stage();
            //khong dong cua so cu ma khoa cua so cu o phía  sau
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reLoad(ActionEvent event) {
        loadUserData(); // ✅ Gọi lại method loadUserData() thay vì gọi DAO
        System.out.println("Dữ liệu đã được cập nhật!");
    }

    @FXML private Button logOutAd;
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
            if (String.valueOf(user.getId()).toLowerCase().contains(keyword) ||
                    user.getEmail().toLowerCase().contains(keyword) ||
                    user.getName().toLowerCase().contains(keyword)) {
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
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Quan_Ly_Product_Admin.fxml");
    }
}
