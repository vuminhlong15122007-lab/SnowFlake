package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

import com.javfxtutorial.hethongdaugia.client.Util.AuctionModificationManager;
import com.javfxtutorial.hethongdaugia.client.Util.ToastNotifier;
import com.javfxtutorial.hethongdaugia.client.Util.UserManager;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.model.Command.LoginCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController implements ResponseListener, Initializable {
  private static final Logger log = LoggerFactory.getLogger(LoginController.class);
  @FXML private TextField Username;
  @FXML private PasswordField Password;
  @FXML private Label message;
  @FXML private NotificationToastController notificationToastController;
  ActionEvent loginEvent;
  @FXML private TextField PasswordVisible;
  private boolean passwordShown = false;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Password.textProperty().bindBidirectional(PasswordVisible.textProperty());
    NetworkManager.getInstance().start();
    AuctionModificationManager.getInstance().start();
    UserManager.getInstance().start();
    Username.setOnAction(event -> Password.requestFocus());
    Password.setOnAction(this::clickLogin);
  }

  @FXML
  public void clickLogin(ActionEvent event) {
    loginEvent = event;
    String username = Username.getText();
    String password;
    password = Password.getText();
    new Thread(
            () -> {
              try {
                Command cmd = new LoginCommand();
                cmd.addData("username", username);
                cmd.addData("password", password);
                NetworkManager.getInstance().sendRequest(cmd, this);
              } catch (Exception e) {
                log.error("Lỗi khi gửi login request: {}", e.getMessage(), e);
                Platform.runLater(
                    () -> {
                      message.setText("Lỗi kết nối: " + e.getMessage());
                      toast().error("Đăng nhập thất bại");
                    });
              }
            })
        .start();
  }

  public void clickCreateAccount(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/SignUp.fxml");
  }

  @Override
  public void onResponse(Response rp) {
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.unregister(rp.getCommand().getClass(), this);
    if (rp.isSuccess()) {
      User user = (User) rp.getPayLoad();
      ClientModel.getInstance().setCurrentUser(user);
      // Load trạng thái đã đọc của user này từ disk ngay sau khi đăng nhập
      ClientModel.getInstance().loadReadNotificationIds(user.getId());
      ClientModel.getInstance().startPruneScheduler();
      Platform.runLater(
          () -> {
            toast().success("Đăng nhập thành công");
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> openHomeScene(user, rp));
            delay.play();
          });
    } else {
      Platform.runLater(
          () -> {
            message.setText("Sai tên hoặc mật khẩu!");
            toast().error("Đăng nhập thất bại");
            log.info(rp.getMessage());
          });
    }
  }

  private void openHomeScene(User user, Response rp) {
    if (user.getAccountType() == AccountType.USER) {
      log.info(rp.getMessage());
      changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
      new HomeController().checkUnpaidAuction();
    } else if (user.getAccountType() == AccountType.ADMIN) {
      log.info(rp.getMessage());
      changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_UserManagement.fxml");
    }
  }

  private ToastNotifier toast() {
    return ToastNotifier.of(notificationToastController);
  }

  // ẩn hiện mkh
  @FXML
  public void togglePasswordVisibility(ActionEvent event) {
    passwordShown = !passwordShown;
    if (passwordShown) {
      PasswordVisible.setVisible(true);
      Password.setVisible(false);
    } else {
      Password.setVisible(true);
      PasswordVisible.setVisible(false);
    }
  }
}
