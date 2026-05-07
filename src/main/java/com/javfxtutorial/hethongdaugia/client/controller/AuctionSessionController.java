package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.sun.jdi.Value;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

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

    // ── Riêng cho cell ENDED ──
    @FXML private Label lbWinner;

    private Auction auction;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) return;
        this.auction = auction;

        // Thông tin chung
        safeSet(lbProductName, auction.getItem().getName());
        safeSet(lbSellerName, auction.getItem().getSellerName());
        safeSet(lbCategory, auction.getItem().getCategory());

        // Load ảnh
        loadImage(auction.getItem().getImage());

        // Xử lý theo trạng thái
        switch (auction.getStatus()) {
            case RUNNING:
                safeSet(statusBadge, "ĐANG DIỄN RA");
                if (statusBadge != null) statusBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 8;");
                safeSet(lbPrice, String.format("%,.0f VND", auction.getCurrentPrice()));
                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setStyle("-fx-background-color: linear-gradient(to right, #56ccf2, #2f80ed); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
                    actionButton.setOnAction(this::clickToLiveAuction);
                }
                break;

            case NOT_START:
                safeSet(statusBadge, "SẮP DIỄN RA");
                if (statusBadge != null) statusBadge.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 8;");
                safeSet(lbPrice, String.format("%,.0f VND", auction.getInitPrice()));
                if (actionButton != null) {
                    actionButton.setDisable(true);
                    actionButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
                }
                break;

            case CLOSED:
                safeSet(lbPrice, String.format("%,.0f VND", auction.getWinningPrice()));
                safeSet(lbCategory, "Loại: " + (auction.getItem().getCategory() != null ? auction.getItem().getCategory() : "Khác"));
                break;
        }
    }

    private String getCategoryName(Item item) {
        String className = item.getClass().getSimpleName();
        switch (className) {
            case "Art": return "Art";
            case "Vehicle": return "Vehicle";
            case "Electronics": return "Electronics";
            default: return "Khác";
        }
    }

    private void safeSet(Label label, String text) {
        if (label != null) label.setText(text);
    }

    private void loadImage(String imageData) {
        if (productImage == null || imageData == null || imageData.isBlank()) return;
        ImageHelper.loadBase64ToImageView(productImage, imageData);
    }

    @FXML
    public void clickToLiveAuction(ActionEvent event) {
        if (auction == null) return;
        ClientModel.getInstance().setCurrentAuction(auction);
        ClientModel.getInstance().setCurrentItem(auction.getItem());
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/man_hinh_hien_thi_sp.fxml");
    }

    @FXML
    public void BtAuction(ActionEvent event) {
        if (auction == null) return;
        ClientModel.getInstance().setCurrentAuction(auction);
        ClientModel.getInstance().setCurrentItem(auction.getItem());
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/dau_gia_truc_tiep.fxml");
    }
}