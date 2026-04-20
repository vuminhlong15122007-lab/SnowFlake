package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

public class HomeController {
    @FXML
    private Button profileButton;
    public void goToProfile(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/man_hinh_hien_thong_tin_User.fxml");
    }

    @FXML
    public void goAuction(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/auction_list.fxml");
    }


    @FXML
    public void goLogin(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    @FXML
    public void manageProducts(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/quan_ly_san_pham_seller.fxml");
    }
}
