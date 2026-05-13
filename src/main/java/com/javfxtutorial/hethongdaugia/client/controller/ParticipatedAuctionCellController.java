package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class ParticipatedAuctionCellController implements ResponseListener {
    @FXML private Button actionButton;
    @FXML private Label lbCategory;
    @FXML private Label lbCurrentPrice;
    @FXML private Label lbProductName;
    @FXML private Label lbWinnerName;
    @FXML private Label lbTime;
    @FXML private StackPane countdownBadge;
    @FXML private ImageView productImage;

    private Auction auction;

    private void showCountdown(LocalDateTime deadline) {
        if (countdownBadge != null) {
            countdownBadge.setVisible(true);
            countdownBadge.setManaged(true);
        }
        if (lbTime != null) {
            TimeLeft timer = new TimeLeft(lbTime, deadline);
            timer.setOnFinished(() -> {
                // Khi hết giờ, đổi badge sang đỏ
                if (lbTime != null)
                    lbTime.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            });
            timer.start();
        }
    }

    private void hideCountdown() {
        if (countdownBadge != null) {
            countdownBadge.setVisible(false);
            countdownBadge.setManaged(false);
        }
    }

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) return;
        this.auction = auction;

        lbProductName.setText(auction.getItem().getName());
        lbCategory.setText(String.valueOf(auction.getItem().getCategory()));
        lbWinnerName.setText(String.valueOf(auction.getWinnerId()));
        lbCurrentPrice.setText(String.valueOf(auction.getCurrentPrice()));

        if (productImage != null
                && auction.getItem().getImage() != null
                && !auction.getItem().getImage().isBlank()) {
            ImageHelper.loadBase64ToImageView(productImage, auction.getItem().getImage());
        }

        switch (auction.getStatus()) {
            case RUNNING:
                hideCountdown();
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                lbWinnerName.setText(String.valueOf(auction.getWinnerId()));
                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setText("THAM GIA");
                    actionButton.setStyle("-fx-background-color: linear-gradient(to right, #56ccf2, #2f80ed);" + "-fx-text-fill: white; -fx-font-weight: bold;" + "-fx-background-radius: 25; -fx-cursor: hand;");
                }
                break;

            case CLOSED:
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                lbWinnerName.setText("Người thắng: " + auction.getWinnerId());
                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setText("THANH TOÁN");
                    actionButton.setStyle(
                            "-fx-background-color: linear-gradient(to right, #e74c3c, #f39c12);" +
                                    "-fx-text-fill: white; -fx-font-weight: bold;" +
                                    "-fx-background-radius: 25; -fx-cursor: hand;");
                }
                // Đếm ngược 24h từ lúc phiên kết thúc
                LocalDateTime deadline = (auction.getEndingTime() != null)
                        ? auction.getEndingTime().plusHours(24)
                        : LocalDateTime.now().plusHours(24);
                showCountdown(deadline);
                break;

            case CANCELLED:
                hideCountdown();
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getInitPrice()));
                lbWinnerName.setText("Phiên bị hủy");
                if (actionButton != null) {
                    actionButton.setDisable(true);
                    actionButton.setText("ĐÃ HỦY");
                    actionButton.setStyle(
                            "-fx-background-color: #bdc3c7; -fx-text-fill: white;" +
                                    "-fx-font-weight: bold; -fx-background-radius: 25;");
                }
                break;

            case PAID:
                hideCountdown();
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getWinningPrice()));
                lbCategory.setText("Loại: " + (auction.getItem().getCategory() != null
                        ? auction.getItem().getCategory() : "Khác"));
                lbWinnerName.setText("Người thắng: " + auction.getWinnerId());
                if (actionButton != null) {
                    actionButton.setDisable(true);
                    actionButton.setText("ĐÃ THANH TOÁN");
                    actionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;" + "-fx-font-weight: bold; -fx-background-radius: 25;");
                }
                lbTime = null;
                break;

            default:
                hideCountdown();
                break;
        }
    }

    @FXML
    public void goPayment(ActionEvent event) {
        if (auction == null) return;
        AuctionStatus status = auction.getStatus();

        if (status == AuctionStatus.CLOSED) {
            openPaymentPopup();
            auction.setStatus(AuctionStatus.PAID);
            NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
            Command cmd = new UpdateAuctionStatusCommand(auction);
            NetworkManager.getConnection().sendCommand(cmd);

        } else if (status == AuctionStatus.RUNNING) {
            changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/LiveAuction.fxml");
        } else if (status == AuctionStatus.PAID) {
            showAlert("Đã thanh toán", "Sản phẩm này đã được thanh toán xong!", "Happy.gif");
        } else {
            showAlert("Không thể thực hiện", "Phiên đấu giá này đã bị hủy.", "Loading.gif");
        }
    }

    private void openPaymentPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/PaymentPopup.fxml"));
            Parent root = loader.load();
            PaymentPopupController popupController = loader.getController();
            popupController.setAuction(auction);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Thanh toán đấu giá");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ thanh toán: " + e.getMessage());
        }
    }

    @Override
    public void onResponse(Response rp) {
        NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);
        if (!rp.isSuccess()){
            Platform.runLater(() -> {
                auction.setStatus(AuctionStatus.CLOSED);
                showAlert("Thanh toán không thành công", "vui lòng thử lại");
            });
        }
    }
}