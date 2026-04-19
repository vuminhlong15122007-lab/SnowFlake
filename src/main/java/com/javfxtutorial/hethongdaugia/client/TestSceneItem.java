package com.javfxtutorial.hethongdaugia.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TestSceneItem extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Test01.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/Quan_Ly_Product_Admin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("SnowFox");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(TestSceneItem.class, args);
    }
}