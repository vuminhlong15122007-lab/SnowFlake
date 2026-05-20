package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class AuctionSessionController {
    // ── Dùng chung cho tất cả cell ──
    @FXML private Label lbProductName;
    @FXML private Label lbSellerName;
    @FXML private Label lbPrice, lbWinner;
    @FXML private Label lbCategory;
    @FXML private Label statusBadge;
    @FXML private ImageView productImage;
    @FXML private Button actionButton;
    @FXML private Label nguoidandau;
    @FXML private Label gia;

    private Auction auction;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) return;
        this.auction = auction;

        // Thông tin chung
        lbProductName.setText(auction.getItem().getName());
        lbSellerName.setText(auction.getItem().getSellerName());
        lbCategory.setText(String.valueOf(auction.getItem().getCategory()));

        lbWinner.setText(
                auction.getWinnerId() != 0 ? auction.getWinnerName() : "Không có người đấu giá");
        // Load ảnh
        if (!(productImage == null
                || auction.getItem().getImage() == null
                || auction.getItem().getImage().isBlank())) {
            ImageHelper.loadBase64ToImageView(productImage, auction.getItem().getImage());
        }

        // Xử lý theo trạng thái
        switch (auction.getStatus()) {
            case RUNNING:
                statusBadge.setText("ĐANG DIỄN RA");
                setStatusBadgeClass("sf-status-running");
                lbPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setText("THAM GIA");
                    setActionButtonClass(actionButton, "sf-auction-action-primary");
                }
                if (nguoidandau != null) nguoidandau.setText("ID dẫn đầu : ");
                if (lbWinner != null) lbWinner.setText("" + auction.getWinnerName());
                gia.setText("Giá hiện tại : ");
                break;

            case NOT_START:
                statusBadge.setText("SẮP DIỄN RA");
                setStatusBadgeClass("sf-status-upcoming");
                lbPrice.setText(String.format("%,.0f VND", auction.getInitPrice()));
                if (actionButton != null) {
                    actionButton.setText("CHƯA BẮT ĐẦU");
                    setActionButtonClass(actionButton, "sf-auction-action-warning");
                }
                gia.setText("Giá khởi điểm : ");
                if (nguoidandau != null) nguoidandau.setText("ID dẫn đầu : ");
                lbWinner.setText(null);
                break;

            case CLOSED:
                statusBadge.setText("ĐÃ KẾT THÚC");
                setStatusBadgeClass("sf-status-ended");
                lbPrice.setText(String.format("%,.0f VND", auction.getWinningPrice()));
                lbCategory.setText(
                        ""
                                + (auction.getItem().getCategory() != null
                                        ? auction.getItem().getCategory()
                                        : "Khác"));
                lbWinner.setText(
                        auction.getWinnerName() != null
                                ? auction.getWinnerName()
                                : "Không có người tham gia đấu giá");
                if (lbWinner != null) lbWinner.setText("" + auction.getWinnerId());
                if (actionButton != null) {
                    actionButton.setText("ĐÃ KẾT THÚC");
                    setActionButtonClass(actionButton, "sf-auction-action-neutral");
                }
                break;
        }
    }

    private void setActionButtonClass(Button button, String styleClass) {
        if (button == null) return;
        button.setStyle("");
        button.getStyleClass()
                .removeAll(
                        "sf-auction-action-primary",
                        "sf-auction-action-warning",
                        "sf-auction-action-neutral",
                        "sf-auction-action-success",
                        "sf-auction-action-danger");
        button.getStyleClass().add(styleClass);
    }

    private void setStatusBadgeClass(String styleClass) {
        if (statusBadge == null) return;
        statusBadge.setStyle("");
        statusBadge
                .getStyleClass()
                .removeAll(
                        "sf-status-badge",
                        "sf-status-running",
                        "sf-status-upcoming",
                        "sf-status-ended");
        statusBadge.getStyleClass().addAll("sf-status-badge", styleClass);
    }

    @FXML
    public void clickToGoToLiveAuction(ActionEvent event) {
        if (auction == null) return;
        ClientModel.getInstance().setCurrentAuction(auction);
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionInformation.fxml");
    }
}
