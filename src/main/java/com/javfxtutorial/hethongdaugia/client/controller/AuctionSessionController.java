package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

public class AuctionSessionController {
    // ── Dùng chung cho tất cả cell ──
    @FXML private Label lbProductName;
    @FXML private Label lbSellerName;
    @FXML private Label lbPrice;
    @FXML private Label lbCategory;
    @FXML private Label statusBadge;
    @FXML private ImageView productImage;
    @FXML private Button actionButton;
    @FXML private Label nguoidandau;
    @FXML private Label gia ;

    // ── Riêng cho cell ENDED ──
    @FXML private Label lbWinner;

    private Auction auction;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) return;
        this.auction = auction;

        // Thông tin chung
        lbProductName.setText(auction.getItem().getName());
        lbSellerName.setText(auction.getItem().getSellerName());
        lbCategory.setText(String.valueOf(auction.getItem().getCategory()));

        lbWinner.setText(auction.getWinnerId() != 0 ? String.valueOf(auction.getWinnerId()) : "Không có người đấu giá");
        // Load ảnh
        if (!(productImage == null || auction.getItem().getImage() == null || auction.getItem().getImage().isBlank()))
        {ImageHelper.loadBase64ToImageView(productImage, auction.getItem().getImage());} ;


        // Xử lý theo trạng thái
        switch (auction.getStatus()) {
            case RUNNING:
                statusBadge.setText("ĐANG DIỄN RA");
                if (statusBadge != null) statusBadge.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 8;");
                lbPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setText("THAM GIA");
                    actionButton.setStyle("-fx-background-color: linear-gradient(to right, #56ccf2, #2f80ed); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");

                }
                if (nguoidandau != null) nguoidandau.setText("ID dẫn đầu : ");
                if (lbWinner != null) lbWinner.setText(""+auction.getWinnerId());
                gia.setText("Giá hiện tại : ");
                break;

            case NOT_START:
                statusBadge.setText("SẮP DIỄN RA");
                if (statusBadge != null) statusBadge.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 8;");
                lbPrice.setText(String.format("%,.0f VND", auction.getInitPrice()));
                if (actionButton != null) {
                    actionButton.setText("CHƯA BẮT ĐẦU");
                    actionButton.setStyle("-fx-background-color: linear-gradient(to right, red, #f39c12); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
                }
                gia.setText("Giá khởi điểm : ");
                if (nguoidandau != null) nguoidandau.setText("ID dẫn đầu : ");
                lbWinner.setText(null);
                break;

            case CLOSED:
                lbPrice.setText(String.format("%,.0f VND", auction.getWinningPrice()));
                lbCategory.setText("Loại: " + (auction.getItem().getCategory() != null ? auction.getItem().getCategory() : "Khác"));
                if (lbWinner != null) lbWinner.setText(""+auction.getWinnerId());
                break;
        }
    }



    @FXML
    public void clickToGoToLiveAuction(ActionEvent event) {
        if (auction == null) return;
        ClientModel.getInstance().setCurrentAuction(auction);
        ClientModel.getInstance().setCurrentItem(auction.getItem());
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionInformation.fxml");
    }
}