package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllItemsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAuctionByItemId;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.javfxtutorial.hethongdaugia.common.model.Item;

import java.io.IOException;
import java.time.LocalDateTime;

public class ManHinhHienThiSpController {
    @FXML private Label EndingtimeLabel;
    @FXML private Label ItemNameLabel;
    @FXML private Label ItemPriceLabel;
    @FXML private Label LbMotasp;
    @FXML private Label StartTimeLabel;
    @FXML private Button ThamGiaDauGiaBtn;
    @FXML private Button btnMenu;
    @FXML private ImageView imgSanPham;
    @FXML private Label lbLoaisp;
    @FXML private Label lbTenngban;
    @FXML private Label lbTimer;


    private Item item = ClientModel.getInstance().getCurrentItem();
    private Auction auction = ClientModel.getInstance().getCurrentAuction();

    public void setData() { // nhan du lieu tu man Item..
        LbMotasp.setText(item.getDescription());
        StartTimeLabel.setText(String.valueOf(auction.getStartingTime()));
        EndingtimeLabel.setText(String.valueOf(auction.getEndingTime()));
        lbTenngban.setText(item.getSellerName());
        ItemNameLabel.setText(item.getName());
        ItemPriceLabel.setText(String.valueOf(auction.getInitPrice()));
    }

    @FXML
    public void initialize() {
        setData();
    }

    @FXML
    public void QuaylaiMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            ClientModel.getInstance().setCurrentAuction(new Auction(1, 1, 1000, 50, LocalDateTime.now(), LocalDateTime.now()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void goToManHinhDauGiaTrucTiep(ActionEvent event) {
        if (auction.getStatus() == AuctionStatus.RUNNING) {
        try {
            System.out.println("Phiên đấu giá hiện tại: " + ClientModel.getInstance().getCurrentAuction());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/dau_gia_truc_tiep.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();}
        } else {
            if (auction.getStatus() == AuctionStatus.CLOSED){
                showAlert("Không thể vào phiên đấu giá", "Đã hết phiên đấu giá");}
            else if (auction.getStatus() == AuctionStatus.NOT_START){
                showAlert("Không thể vào phiên đấu giá", "Chưa bắt đầu phiên đấu giá");}
        }
    }
    //hien thi alert
    public void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

