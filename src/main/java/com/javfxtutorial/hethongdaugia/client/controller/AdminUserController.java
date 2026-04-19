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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class AdminUserController implements  Initializable {
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

    private UserDAO nguoiDungDAO = UserDAO.getInstance();
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
    private void loadUserData(){
        danhSach = FXCollections.observableArrayList(nguoiDungDAO.selectAll());
        userTable.setItems(danhSach);
    }
    //cap nhat thong tin
    @FXML
    private Button btnEditUser;
    @FXML
    public void getUpdateAdmin(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/update_admin.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();

            // 1. (Tùy chọn) Thiết lập tính năng Modality
            // Nếu bạn muốn người dùng bắt buộc phải thao tác và đóng pop-up này
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/Popupmanhinhsuathongtinadmin.fxml"));
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
    public void clickToDeleteUser() throws IOException {
        ServerConnection connection = new ServerConnection();
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


    public void reLoad(ActionEvent event){
        userTable.getItems().clear();
        List<User> freshData = nguoiDungDAO.selectAll();
        userTable.getItems().addAll(freshData);
        System.out.println("Dữ liệu đã được cập nhật!");

    }
    @FXML private Button logOutAd;
    @FXML
    public void logOut(ActionEvent event) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    //tim kiem
    @FXML
    public void clickToSearch(){
        String textWord = searchField.getText();

        if(textWord == null || textWord.trim().isEmpty()){
            userTable.setItems(danhSach);
            return;
        }
        //tao danh sach ket qua
        ObservableList<User> result = FXCollections.observableArrayList();
        String keyword = textWord.toLowerCase().trim();

        for (User user : danhSach){
            if(String.valueOf(user.getId()).toLowerCase().contains(keyword) ||
                    user.getEmail().toLowerCase().contains(keyword) ||
                    user.getName().toLowerCase().contains(keyword)){
                result.add(user);
            }
        }
        userTable.setItems(result);

        System.out.println("Đã tìm thấy " + result.size() + " kết quả");
    }
    //xoa tim kiem
    public void clickToDeleteSearch(){
        searchField.clear();
        userTable.setItems(danhSach);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}