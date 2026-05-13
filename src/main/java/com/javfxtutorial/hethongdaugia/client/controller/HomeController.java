package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

public class HomeController {
    @FXML
    public void goToProfile(ActionEvent event){
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/UserInformation.fxml");
    }

    @FXML
    public void goAuction(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
    }


    @FXML
    public void goLogin(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    @FXML
    public void manageProducts(ActionEvent event){
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductManagement.fxml");
    }
}
