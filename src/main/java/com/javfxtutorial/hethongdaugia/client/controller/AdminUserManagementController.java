package com.javfxtutorial.hethongdaugia.client.controller;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteUserCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
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
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class AdminUserManagementController implements  Initializable {
    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colPhone;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colStatus;
    @FXML
    private Button deleteButton;

    private UserDAO nguoiDungDAO = new UserDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tên trong ngoặc kép ("id", "username", "email") phải CHÍNH XÁC với tên biến trong class User
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("accountType"));

        // Lấy dữ liệu và nhét vào bảng
        loadUserData();
    }
    @FXML
    private void loadUserData(){
        ObservableList<User> danhSach = FXCollections.observableArrayList(nguoiDungDAO.selectAll());
        userTable.setItems(danhSach);
    }
    //cap nhat thong tin
    @FXML
    private Button btnEditUser;
    @FXML
    public void getUpdateUser(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/Popupmanhinhsuathongtinadmin.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();

            // 1. (Tùy chọn) Thiết lập tính năng Modality
            // Nếu bạn muốn người dùng bắt buộc phải thao tác và đóng pop-up này
            // trước khi có thể click vào màn hình chính bên dưới, hãy bỏ comment dòng sau:
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // 2. (Tùy chọn) Đặt tiêu đề cho pop-up
            popupStage.setTitle("Thêm User mới");

            // 3. Khởi tạo Scene và gắn vào Stage mới
            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            // 4. Hiển thị pop-up
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void clickButtonExit(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void clickToAddUser(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/Popupmanhinhsuathongtinadmin.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();

            // 1. (Tùy chọn) Thiết lập tính năng Modality
            // Nếu bạn muốn người dùng bắt buộc phải thao tác và đóng pop-up này
            // trước khi có thể click vào màn hình chính bên dưới, hãy bỏ comment dòng sau:
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // 2. (Tùy chọn) Đặt tiêu đề cho pop-up
            popupStage.setTitle("Thêm User mới");

            // 3. Khởi tạo Scene và gắn vào Stage mới
            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            // 4. Hiển thị pop-up
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private Button btnDeleteUser;
    @FXML
    public void clickToDeleteUser(){
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
        //gan 2 nut vao thay ok, cancle mac dinh
        confirmAlert.getButtonTypes().setAll(yes, no);
        //cho nguoi dung bam
        ButtonType result = confirmAlert.showAndWait().orElse(null);
        //neu co
        if(result == yes){
            //tao command gui len server
            ServerConnection connection = new ServerConnection();
            DeleteUserCommand cmd = new DeleteUserCommand();
            cmd.addData("userId", selectUser.getId());
            cmd.addData("username", selectUser.getName());
            cmd.addData("email", selectUser.getEmail());
            cmd.addData("phone", selectUser.getSdt());

            Response rp = connection.sendCommand(cmd);
            if(rp.isSuccess()){
                showAlert("Xóa thành công", rp.getMessage());
                loadUserData();//load lai bang
            }else{
                showAlert("Lỗi", rp.getMessage());
            }

        }

    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private Button btnResetPassword;
    @FXML
    public void clickToResetPW(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/reset_password.fxml"));

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDeleteUser() {
        // 1. Lấy dòng đang được chọn từ TableView
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        UserDAO  dao = new UserDAO();

        if (selectedUser != null) {
            // 2. Hiển thị hộp thoại xác nhận (Tùy chọn nhưng nên có)
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận xóa");
            alert.setHeaderText("Bạn có chắc chắn muốn xóa tài khoản: " + selectedUser.getName() + "?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // 3. Thực hiện xóa trong Cơ sở dữ liệu (ví dụ gọi hàm delete từ DAO)
                int success = dao.delete(selectedUser);

                if (success != 0) {
                    // 4. Cập nhật lại giao diện (Xóa khỏi ObservableList đang hiển thị)
                    userTable.getItems().remove(selectedUser);
                    System.out.println("Xóa thành công!");
                } else {
                    System.out.println("Lỗi hệ thống không thể xóa.");
                }
            }
        } else {
            // Nếu chưa chọn hàng nào mà đã bấm nút
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setContentText("Vui lòng chọn một tài khoản trong danh sách trước!");
            warning.show();
        }
    }

}