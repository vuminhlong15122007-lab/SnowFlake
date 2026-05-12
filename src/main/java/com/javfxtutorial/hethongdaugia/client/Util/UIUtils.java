package com.javfxtutorial.hethongdaugia.client.Util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class UIUtils {
    // method để hiện method Alert
    // Dùng icon mặc định
    public static void showAlert(String title, String message) {
        showAlert(title, message, "/com/javfxtutorial/hethongdaugia/assets/Logo.png", "/com/javfxtutorial/hethongdaugia/assets/Fox.gif");
    }

    //  Cho phép đổi Meme
    public static void showAlert(String title, String message, String meme) {
        String memePath = "/com/javfxtutorial/hethongdaugia/assets/" + meme;
        showAlert(title, message, "/com/javfxtutorial/hethongdaugia/assets/Logo.png", memePath);
    }

    // Full ảnh + meme
    public static void showAlert(String title, String message, String iconPath, String memePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Xử lý thay đổi Meme (Dấu ! mặc định)
        if (memePath != null) {
            try {
                Image memeImg = new Image(UIUtils.class.getResourceAsStream(memePath));
                ImageView imageView = new ImageView(memeImg);
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            } catch (Exception e) {
                System.err.println("Không load được meme tại: " + memePath);
            }
        }

        // Xử lý thay đổi Icon nhỏ trên thanh tiêu đề
        if (iconPath != null) {
            try {
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(UIUtils.class.getResourceAsStream(iconPath)));
            } catch (Exception e) {
                System.err.println("Không load được icon tại: " + iconPath);
            }
        }

        alert.showAndWait();
    }

    //  method để hiện ra lỗi
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // method để chuyển màn hình
    public static void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource(fxmlPath));
            Parent root = loader.load(); // Lỗi thường nằm ở dòng này
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace(); // Xem chi tiết lỗi trong Console
            showAlert("Lỗi", "Không thể tải màn hình: " + fxmlPath);
        }
    }
    // method đóng cửa sổ hiện tại , cái này dành cho cửa sổ Popup
    public static void closeCurrentWindow(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }
    public static void changePopup(ActionEvent event , String fxmlPath , String typePopUp){
        try {
            FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle(typePopUp);
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
