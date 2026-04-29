package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllUsersCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class AuctionController implements ResponseListener {
    @FXML private ListView<Auction> featuredProductList;
    @FXML private TextField searchField;
    @FXML private Button btnHome;
    @FXML private Button btnLiveAuction;
    @FXML private Button btnmanageProducts;
    @FXML private Button btnprofile;

    private final ObservableList<Auction> observable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        VBox.setVgrow(featuredProductList, Priority.ALWAYS);
        featuredProductList.setMaxWidth(Double.MAX_VALUE);
        featuredProductList.setCellFactory(lv -> new ProductCell());

        FilteredList<Auction> filterData = new FilteredList<>(observable, auction -> true);
        searchField.textProperty().addListener((obs, oldValue, newValue) ->
                filterData.setPredicate(auction -> {
                    if (newValue == null || newValue.isBlank()) {
                        return true;
                    }
                    if (auction == null || auction.getItem() == null || auction.getItem().getName() == null) {
                        return false;
                    }
                    return auction.getItem().getName().toLowerCase().contains(newValue.toLowerCase());
                })
        );

        featuredProductList.setItems(filterData);
        loadData();
    }

    public void loadData() {
        Command cmd = new GetAllAuctionsCommand();
        ServerConnection connection = ServerConnection.getInstance();
        new Thread(() -> {
            connection.sendCommand(cmd);
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.register(GetAllAuctionsCommand.class, this);
        }).start();
    }

    public void logOut1(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    public void manageProducts(ActionEvent event) {
            changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/quan_ly_san_pham_seller.fxml");
    }

    @FXML
    public void btnHome(ActionEvent event) {
        try {
            changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToProfile(ActionEvent event) {
            changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/man_hinh_hien_thong_tin_User.fxml");
    }

    @Override
    public void onResponse(Response rp) {
        Platform.runLater(() -> {
            if (rp == null) {
                showAlert("Loi tai du lieu", "Server khong tra ve du lieu phien dau gia." , "Loading.gif");
                return;
            }

            if (!rp.isSuccess()) {
                showAlert("Loi tai du lieu", rp.getMessage() , "Loading.gif");
                return;
            }

            Object payload = rp.getPayLoad();
            if (!(payload instanceof ArrayList<?> payloadList)) {
                showAlert("Loi tai du lieu", "Du lieu tra ve khong dung dinh dang." , "Loading.gif");
                return;
            }

            ArrayList<Auction> auctions = new ArrayList<>();
            for (Object item : payloadList) {
                if (item instanceof Auction auction) {
                    auctions.add(auction);
                }
            }
            observable.setAll(auctions);
        });

        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(GetAllAuctionsCommand.class, this);
    }
}
