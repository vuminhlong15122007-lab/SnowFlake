package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
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

public class AuctionController {
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
        ServerConnection connection = new ServerConnection();

        new Thread(() -> {
            try {
                connection.sendCommand(cmd);
                Response resp = connection.receiveResponse();

                Platform.runLater(() -> {
                    if (resp == null) {
                        showAlert("Loi tai du lieu", "Server khong tra ve du lieu phien dau gia.");
                        return;
                    }

                    if (!resp.isSuccess()) {
                        showAlert("Loi tai du lieu", resp.getMessage());
                        return;
                    }

                    Object payload = resp.getPayLoad();
                    if (!(payload instanceof ArrayList<?> payloadList)) {
                        showAlert("Loi tai du lieu", "Du lieu tra ve khong dung dinh dang.");
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
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Khong the load phien dau gia", "Kiem tra server localhost:5000 va log loi trong console."));
            } finally {
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        }).start();
    }

    public void logOut1(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
