package com.javfxtutorial.hethongdaugia.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("SnowFox");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
