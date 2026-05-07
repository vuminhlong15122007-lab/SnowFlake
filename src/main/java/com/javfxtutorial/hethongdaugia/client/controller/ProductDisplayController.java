package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.scene.layout.VBox;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class ProductDisplayController {
    @FXML private Label EndingtimeLabel;
    @FXML private Label ItemNameLabel;
    @FXML private Label ItemPriceLabel;
    @FXML private Label LbMotasp;
    @FXML private Label StartTimeLabel;
    @FXML private ImageView itemImageView;
    @FXML private Label lbTenngban;
    @FXML private Label lbtimeLeft;
    @FXML private Label UI01;
    @FXML private VBox UI02;
    @FXML private Button ThamGiaDauGiaBtn;
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
            UI01.setStyle("-fx-text-fill: orange; -fx-alignment: CENTER;");
            UI02.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: orange; -fx-alignment: CENTER;");
            lbtimeLeft.setStyle("-fx-text-fill: orange;");
            ThamGiaDauGiaBtn.setText("Chưa thể tham gia");
            ThamGiaDauGiaBtn.setStyle("-fx-background-color: linear-gradient(to right, red, orange); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-cursor: hand;");
        } else {
            lbtimeLeft.setText("ĐÃ KẾT THÚC");
            UI01.setStyle("-fx-text-fill: red; -fx-alignment: CENTER;");
            UI02.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: red; -fx-alignment: CENTER;");
            lbtimeLeft.setStyle("-fx-text-fill: red;");
            ThamGiaDauGiaBtn.setText("Phiên đấu giá đã đóng");
            ThamGiaDauGiaBtn.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-cursor: hand;");
        }
    }

    @FXML
    public void QuaylaiMenu(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
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

