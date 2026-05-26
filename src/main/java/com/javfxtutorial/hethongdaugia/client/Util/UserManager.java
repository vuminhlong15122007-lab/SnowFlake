package com.javfxtutorial.hethongdaugia.client.Util;

import com.javfxtutorial.hethongdaugia.client.MainApp;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteUserCommand;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class UserManager implements ResponseListener {
  private static UserManager instance;

  private UserManager() {
  }
  public static UserManager getInstance() {
    if (instance == null) {
      instance = new UserManager();
    }
    return instance;
  }

  public void start() {
    NetworkManager.getInstance().register(DeleteUserCommand.class, this);
     }

  @Override
  public void onResponse(Response rp) {
    if (rp.isSuccess()){
      int userId = (int) rp.getPayLoad();
      if (userId == ClientModel.getInstance().getCurrentUser().getId()) {
        Platform.runLater(() -> {
          showAlert("Xóa tài khoản", "Tài khoản của bạn đã bị xóa");
          try {
            Stage stage = (Stage) Stage.getWindows()
                .stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);

            if (stage != null) {
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
              stage.setScene(new Scene(loader.load()));
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
      }
    }
  }
}
