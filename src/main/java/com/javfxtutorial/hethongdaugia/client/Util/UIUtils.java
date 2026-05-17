package com.javfxtutorial.hethongdaugia.client.Util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class UIUtils {
    public static void showAlert(String title, String message) {
        showAlert(title, message, "/com/javfxtutorial/hethongdaugia/assets/Logo.png", "/com/javfxtutorial/hethongdaugia/assets/Fox.gif");
    }

    public static void showAlert(String title, String message, String meme) {
        String memePath = "/com/javfxtutorial/hethongdaugia/assets/" + meme;
        showAlert(title, message, "/com/javfxtutorial/hethongdaugia/assets/Logo.png", memePath);
    }

    public static void showAlert(String title, String message, String iconPath, String memePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, iconPath);

        if (memePath != null) {
            try {
                Image memeImg = new Image(UIUtils.class.getResourceAsStream(memePath));
                ImageView imageView = new ImageView(memeImg);
                imageView.setFitHeight(118);
                imageView.setFitWidth(118);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            } catch (Exception e) {
                System.err.println("Khong load duoc meme tai: " + memePath);
            }
        }

        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, "/com/javfxtutorial/hethongdaugia/assets/Logo.png");
        alert.showAndWait();
    }

    private static void styleAlert(Alert alert, String iconPath) {
        alert.initStyle(StageStyle.TRANSPARENT);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setPrefWidth(430);
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, -sf-page-start, -sf-surface 50%, -sf-purple-soft);" +
                "-fx-background-radius: 24;" +
                "-fx-border-color: -sf-border;" +
                "-fx-border-radius: 24;" +
                "-fx-border-width: 1.4;" +
                "-fx-padding: 22;"
        );

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Đồng ý");
        okButton.setStyle(
                "-fx-background-color: linear-gradient(to right, -sf-accent-2, -sf-border);" +
                "-fx-background-radius: 18;" +
                "-fx-text-fill: -sf-text;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 28;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, -sf-shadow, 12, 0.22, 0, 3);"
        );

        alert.setOnShowing(_ -> {
            Scene scene = dialogPane.getScene();
            if (scene != null) {
                ThemeManager.apply(scene);
                scene.setFill(Color.TRANSPARENT);
                if (scene.getWindow() instanceof Stage stage) {
                    stage.setResizable(false);
                    if (iconPath != null) {
                        try {
                            stage.getIcons().add(new Image(UIUtils.class.getResourceAsStream(iconPath)));
                        } catch (Exception e) {
                            System.err.println("Khong load duoc icon tai: " + iconPath);
                        }
                    }
                }
            }

            Node contentLabel = dialogPane.lookup(".content.label");
            if (contentLabel != null) {
                contentLabel.setStyle(
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: -sf-text;" +
                        "-fx-font-weight: bold;" +
                        "-fx-wrap-text: true;"
                );
            }
        });
    }

    public static void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.apply(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải màn hình: " + fxmlPath);
        }
    }

    public static void closeCurrentWindow(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    public static void changePopup(ActionEvent event, String fxmlPath, String typePopUp) {
        try {
            FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle(typePopUp);
            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            popupStage.setScene(scene);
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
