package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.javfxtutorial.hethongdaugia.common.model.Item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class ProductDisplayController {
    @FXML private Label EndingtimeLabel;
    @FXML private Label ItemNameLabel;
    @FXML private Label ItemPriceLabel;
    @FXML private Label LbMotasp;
    @FXML private Label StartTimeLabel;
    @FXML private Button ThamGiaDauGiaBtn;
    @FXML private Button btnMenu;
    @FXML private ImageView itemImageView;
    @FXML private Label lbLoaisp;
    @FXML private Label lbTenngban;
    @FXML private Label lbtimeLeft;
    private TimeLeft timer;


    private Item item = ClientModel.getInstance().getCurrentItem();
    private Auction auction = ClientModel.getInstance().getCurrentAuction();

    public void setData() { // nhan du lieu tu man Item..
        LbMotasp.setText(item.getDescription());
        StartTimeLabel.setText(String.valueOf(auction.getStartingTime()));
        EndingtimeLabel.setText(String.valueOf(auction.getEndingTime()));
        lbTenngban.setText(item.getSellerName());
        ItemNameLabel.setText(item.getName());
        ItemPriceLabel.setText(String.format("%,.0f VND", auction.getCurrentPrice()));

        String base64Data = auction.getItem().getImage();
        ImageHelper.loadBase64ToImageView(itemImageView,base64Data);
    }

    @FXML
    public void initialize() {
        setData();
        if (auction.getStatus().toString().equals("RUNNING")) {
            timer = new TimeLeft(lbtimeLeft, auction.getEndingTime());
            timer.start();
        }else if ( auction.getStatus().toString().equals("NOT_START")) {
            lbtimeLeft.setText("CHƯA BẮT ĐẦU");
            lbtimeLeft.setStyle("-fx-text-fill: #888888;");
        } else {
            lbtimeLeft.setText("ĐÃ KẾT THÚC");
            lbtimeLeft.setStyle("-fx-text-fill: #888888;");
        }
    }

    @FXML
    public void QuaylaiMenu(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/auction_list.fxml");
    }


    @FXML
    public void goToManHinhDauGiaTrucTiep(ActionEvent event) {
        if (auction.getStatus() == AuctionStatus.RUNNING) {
            System.out.println("Phiên đấu giá hiện tại: " + ClientModel.getInstance().getCurrentAuction());
            changeScene(event ,"/com/javfxtutorial/hethongdaugia/view/fxml/dau_gia_truc_tiep.fxml");

        } else {
            if (auction.getStatus() == AuctionStatus.CLOSED){
                showAlert("Không thể vào phiên đấu giá", "Đã hết phiên đấu giá");}
            else if (auction.getStatus() == AuctionStatus.NOT_START){
                showAlert("Không thể vào phiên đấu giá", "Chưa bắt đầu phiên đấu giá");}
        }
    }
}

