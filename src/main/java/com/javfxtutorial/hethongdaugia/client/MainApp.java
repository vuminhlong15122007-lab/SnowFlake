package com.javfxtutorial.hethongdaugia.client;

import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
  public static Stage stage;
  @Override
  public void start(Stage stage) throws IOException {
    MainApp.stage = stage;
    FXMLLoader fxmlLoader =
        new FXMLLoader(
            MainApp.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    ThemeManager.apply(scene);
    stage.setTitle("SnowFox");
    stage.setScene(scene);
    stage.show();
    stage.setOnCloseRequest(e -> {
      Platform.exit();
      System.exit(0); // thoát hẳn JVM nếu cần
    });
  }

  public static void main(String[] args) {
    Application.launch(MainApp.class, args);
  }
}
