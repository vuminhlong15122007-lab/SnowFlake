package com.javfxtutorial.hethongdaugia.client;

import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainTestApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader =
                new FXMLLoader(
                        MainTestApp.class.getResource(
                                "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ThemeManager.apply(scene);
        stage.setTitle("SnowFox");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(MainTestApp.class, args);
    }
}
