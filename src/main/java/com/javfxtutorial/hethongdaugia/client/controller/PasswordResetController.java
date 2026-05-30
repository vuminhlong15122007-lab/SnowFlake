package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ToastNotifier;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.ResetPassWordCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordResetController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
  @FXML private TextField txtNewPW;
  @FXML private TextField txtConfirmPW;
  @FXML private Button btnCancel;
  @FXML private NotificationToastController notificationToastController;

  @FXML
  public void initialize() {
    btnCancel.setOnAction(
        _ -> {
          // Llay va dong stage hien tai
          Stage stage = (Stage) btnCancel.getScene().getWindow();
          stage.close();
        });
    loadUserInfo();
  }

  // lay du lieu tu clientmodel de hien thi
  @FXML
  public void loadUserInfo() {
    User currentUser = ClientModel.getInstance().getCurrentUser();
    if (currentUser != null) {
      txtNewPW.setText("");
      txtConfirmPW.setText("");
    }
  }

  User currentUser;
  String newPW;

  @FXML
  public void updatePW() {
    // lay du lieu tu o nhap
    newPW = txtNewPW.getText();
    String confirmPW = txtConfirmPW.getText();

    if (newPW == null || newPW.isBlank() || confirmPW == null || confirmPW.isBlank()) {
      notifyWarning("Vui lòng nhập đầy đủ mật khẩu");
      return;
    }
    if (!newPW.equals(confirmPW)) {
      notifyWarning("Mật khẩu xác nhận không khớp");
      return;
    }

    // lay user hien tai

    currentUser = ClientModel.getInstance().getCurrentUser();
    if (currentUser == null) {
      notifyError("Chưa đăng nhập");
      return;
    }

    // tao command gui len server
    new Thread(
            () -> {
              try {
                ResetPassWordCommand cmd = new ResetPassWordCommand();
                cmd.addData("userId", currentUser.getId());
                cmd.addData("passWord", newPW);

                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.sendRequest(cmd, this);
              } catch (ConnectionFailedException e) {
                log.error("Lỗi kết nối: {}", e.getMessage());
                Platform.runLater(() -> notifyError("Không thể kết nối đến server"));
              } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(() -> notifyError("Không thể gửi yêu cầu đổi mật khẩu"));
              } catch (Exception e) {
                log.error("Lỗi reset password: {}", e.getMessage(), e);
                Platform.runLater(() -> notifyError("Đổi mật khẩu thất bại: " + e.getMessage()));
              }
            })
        .start();
  }

  @Override
  public void onResponse(Response rp) {
    if (rp.isSuccess()) {
      currentUser.setPassWord(newPW);
      // log.debug("PW từ server: {}", newUser.getPassWord());

      // load lai man hinh
      loadUserInfo();
      Platform.runLater(() -> notifySuccess("Cập nhật mật khẩu thành công"));

    } else {
      Platform.runLater(() -> notifyError(rp.getMessage()));
    }
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.unregister(ResetPassWordCommand.class, this);
  }

  private void notifySuccess(String message) {
    toast().success(message);
  }

  private void notifyWarning(String message) {
    toast().warning(message);
  }

  private void notifyError(String message) {
    toast().error(message);
  }

  private ToastNotifier toast() {
    return ToastNotifier.of(notificationToastController);
  }
}
