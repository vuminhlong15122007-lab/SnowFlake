package com.javfxtutorial.hethongdaugia.client.controller;

import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class TermsController {

    @FXML private ScrollPane scrollPane;

    // Callback để báo kết quả về RegisterController
    private Consumer<Boolean> resultCallback;

    public void setResultCallback(Consumer<Boolean> callback) {
        this.resultCallback = callback;
    }

    // Người dùng bấm "Đồng ý"
    @FXML
    public void onAgree(ActionEvent event) {
        if (resultCallback != null) {
            resultCallback.accept(true);
        }
        closeWindow();
    }

    // Người dùng bấm "Từ chối"
    @FXML
    public void onDecline(ActionEvent event) {
        if (resultCallback != null) {
            resultCallback.accept(false);
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        stage.close();
    }
}
