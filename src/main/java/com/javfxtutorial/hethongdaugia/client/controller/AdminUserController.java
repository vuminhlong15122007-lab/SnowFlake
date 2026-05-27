package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.*;

import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import com.javfxtutorial.hethongdaugia.client.Util.ToastNotifier;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteUserCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllUsersCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminUserController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

  @FXML private TableView<User> userTable;
  @FXML private TableColumn<User, Integer> colId;
  @FXML private TableColumn<User, String> colUsername;
  @FXML private TableColumn<User, String> colEmail;
  @FXML private TableColumn<User, String> colPhone;
  @FXML private TableColumn<User, String> colRole;
  @FXML private TextField searchField;
  @FXML private Label userCountBadge;
  @FXML private NotificationToastController notificationToastController;

  private User selectUser;
  private ObservableList<User> danhSach;

  private void updateCountBadge() {
    if (userCountBadge == null) return;
    int total = (danhSach != null) ? danhSach.size() : 0;
    int showing = userTable.getItems().size();

    if (showing == total) {
      userCountBadge.setText(total + " người dùng");
    } else {
      userCountBadge.setText(showing + " / " + total + " người dùng");
    }
  }

  @FXML
  public void initialize() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colUsername.setCellValueFactory(new PropertyValueFactory<>("name"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colPhone.setCellValueFactory(new PropertyValueFactory<>("sdt"));
    colRole.setCellValueFactory(new PropertyValueFactory<>("accountType"));
    if (userCountBadge != null) {
      userCountBadge.setText("Đang tải...");
    }

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
                    () -> {
                      toast().error("Không thể kết nối đến server");
                      if (userCountBadge != null) userCountBadge.setText("Lỗi kết nối");
                    });
              } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(
                    () -> {
                      toast().error("Không thể gửi yêu cầu");
                      if (userCountBadge != null) userCountBadge.setText("Lỗi");
                    });
              } catch (Exception e) {
                log.error("Lỗi tải user: {}", e.getMessage(), e);
                Platform.runLater(
                    () -> {
                      toast().error("Tải dữ liệu thất bại");
                      if (userCountBadge != null) userCountBadge.setText("Lỗi");
                    });
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
  public void clickToDeleteUser() {
    selectUser = userTable.getSelectionModel().getSelectedItem();
    if (selectUser == null) {
      toast().warning("Vui lòng chọn người dùng cần xóa");
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

    if (result == yes) {
      DeleteUserCommand cmd = new DeleteUserCommand();
      cmd.addData("userId", selectUser.getId());
      cmd.addData("username", selectUser.getName());
      cmd.addData("email", selectUser.getEmail());
      cmd.addData("phone", selectUser.getSdt());

      NetworkManager.getInstance().register(DeleteUserCommand.class, this);

      new Thread(
              () -> {
                try {
                  NetworkManager.getConnection().sendCommand(cmd);
                } catch (Exception e) {
                  log.error("Lỗi gửi DeleteUserCommand: {}", e.getMessage(), e);
                  NetworkManager.getInstance().unregister(DeleteUserCommand.class, this);
                  Platform.runLater(() -> toast().error("Không thể gửi yêu cầu"));
                }
              })
          .start();
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
      log.error("Lỗi mở popup reset password: {}", e.getMessage(), e);
      toast().error("Không thể mở cửa sổ đặt lại mật khẩu");
    }
  }

  public void reLoad() {
    if (userCountBadge != null) userCountBadge.setText("Đang tải...");
    loadUserData();
    log.info("Dữ liệu đã được cập nhật!");
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
      updateCountBadge(); // hiển thị tổng đầy đủ
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
    updateCountBadge(); // hiển thị "X / Y người dùng"
    log.info("Tìm kiếm '{}' → {} kết quả", keyword, result.size());
  }

  public void clickToDeleteSearch() {
    searchField.clear();
    userTable.setItems(danhSach);
    updateCountBadge(); // về lại tổng đầy đủ
  }

  @FXML
  public void clickToGoItemAdmin(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_ProductManagement.fxml");
  }

  @Override
  public void onResponse(Response rp) {

    // ── Xóa user ──
    if (rp.getCommand().getClass() == DeleteUserCommand.class) {
      Platform.runLater(
          () -> {
            if (rp.isSuccess()) {
              toast().success(rp.getMessage());
              // Xóa khỏi cả danh sách gốc lẫn table
              userTable.getItems().remove(selectUser);
              if (danhSach != null) danhSach.remove(selectUser);
              updateCountBadge(); // cập nhật badge sau khi xóa
            } else {
              toast().error(rp.getMessage());
            }
          });
      NetworkManager.getInstance().unregister(DeleteUserCommand.class, this);
    }

    // ── Tải danh sách ──
    if (rp.getCommand().getClass() == GetAllUsersCommand.class) {
      if (rp.isSuccess()) {
        List<User> users = (List<User>) rp.getPayLoad();
        Platform.runLater(
            () -> {
              danhSach = FXCollections.observableArrayList(users);
              userTable.setItems(danhSach);
              updateCountBadge(); // cập nhật badge sau khi tải xong
            });
      } else {
        Platform.runLater(
            () -> {
              toast().error(rp.getMessage());
              if (userCountBadge != null) userCountBadge.setText("0 người dùng");
            });
      }
      NetworkManager.getInstance().unregister(GetAllUsersCommand.class, this);
    }
  }

  private ToastNotifier toast() {
    return ToastNotifier.of(notificationToastController);
  }
}
