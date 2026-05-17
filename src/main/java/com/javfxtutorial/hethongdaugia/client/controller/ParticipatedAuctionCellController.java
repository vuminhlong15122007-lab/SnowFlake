package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class ParticipatedAuctionCellController implements ResponseListener {
    private static final Logger log = LoggerFactory.getLogger(ParticipatedAuctionCellController.class);

    @FXML private Button actionButton;
    @FXML private Label lbCategory;
    @FXML private Label lbCurrentPrice;
    @FXML private Label lbProductName;
    @FXML private Label lbWinnerName;
    @FXML private Label lbTime;
    @FXML private StackPane countdownBadge;
    @FXML private ImageView productImage;

    private Auction auction;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            return;
        }

        this.auction = auction;

        lbProductName.setText(auction.getItem().getName());
        lbCategory.setText(String.valueOf(auction.getItem().getCategory()));
        lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbWinnerName.setText(String.valueOf(auction.getWinnerId()));

        if (productImage != null
                && auction.getItem().getImage() != null
                && !auction.getItem().getImage().isBlank()) {
            ImageHelper.loadBase64ToImageView(productImage, auction.getItem().getImage());
        }

        updateUI(auction.getStatus());

        auction.statusProperty().addListener((obs, oldVal, newVal) -> updateUI(newVal));
    }

    private void showCountdown(LocalDateTime deadline) {
        if (countdownBadge != null) {
            countdownBadge.setVisible(true);
            countdownBadge.setManaged(true);
        }

        if (lbTime == null) {
            return;
        }

        TimeLeft timer = new TimeLeft(lbTime, deadline);
        timer.setOnFinished(() -> {
            if (lbTime != null) {
                lbTime.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-text-fill: -sf-danger;"
                );
            }

            if (auction == null) {
                return;
            }

            AuctionStatus currentStatus = auction.getStatus();

            if (currentStatus == AuctionStatus.RUNNING) {
                updateAuctionStatus(AuctionStatus.CLOSED);
            } else if (currentStatus == AuctionStatus.CLOSED) {
                updateAuctionStatus(AuctionStatus.CANCELLED);
            }
        });
        timer.start();
    }

    private void hideCountdown() {
        if (countdownBadge != null) {
            countdownBadge.setVisible(false);
            countdownBadge.setManaged(false);
        }
    }

    @FXML
    public void goPayment(ActionEvent event) {
        if (auction == null) {
            return;
        }

        AuctionStatus status = auction.getStatus();

        if (status == AuctionStatus.CLOSED) {
            openPaymentPopup();
        } else if (status == AuctionStatus.RUNNING) {
            ClientModel.getInstance().setCurrentAuction(auction);
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
                    getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/PaymentPopup.fxml")
            );

            Parent root = loader.load();
            PaymentPopupController popupController = loader.getController();
            popupController.setAuction(auction);

            popupController.setOnConfirmed(() -> updateAuctionStatus(AuctionStatus.PAID));

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            ThemeManager.apply(scene);

            popupStage.setScene(scene);
            popupStage.show();
        } catch (Exception e) {
            log.error("Không thể mở cửa sổ thanh toán", e);
            showAlert("Lỗi", "Không thể mở cửa sổ thanh toán: " + e.getMessage());
        }
    }

    private void updateAuctionStatus(AuctionStatus newStatus) {
        if (auction == null || newStatus == null) {
            return;
        }

        AuctionStatus oldStatus = auction.getStatus();
        auction.setStatus(newStatus);

        NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);

        Platform.runLater(() -> {
            try {
                ServerConnection connection = NetworkManager.getConnection();
                Command cmd = new UpdateAuctionStatusCommand(auction);
                connection.sendCommand(cmd);
            } catch (ConnectionFailedException e) {
                auction.setStatus(oldStatus);
                log.error("Không kết nối được server", e);
                showAlert("Lỗi", e.getMessage());
            } catch (SendFailedException e) {
                auction.setStatus(oldStatus);
                log.error("Không gửi được command", e);
                showAlert("Lỗi", e.getMessage());
            }
        });
    }

    private void updateUI(AuctionStatus status) {
        if (auction == null || status == null) {
            hideCountdown();
            return;
        }

        switch (status) {
            case RUNNING:
                hideCountdown();
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                lbWinnerName.setText(String.valueOf(auction.getWinnerId()));

                if (actionButton != null) {
                    actionButton.setDisable(false);
                    actionButton.setText("THAM GIA");
                    actionButton.setStyle(
                            "-fx-background-color: linear-gradient(to right, -sf-accent-2, -sf-accent);" +
                                    "-fx-text-fill: -sf-on-accent;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 25;" +
                                    "-fx-cursor: hand;"
                    );
                }
                break;

            case CLOSED:
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
                lbWinnerName.setText("Người thắng: " + auction.getWinnerId());

                int userId = ClientModel.getInstance().getCurrentUser().getId();
                if (actionButton != null) {
                    if (auction.getWinnerId() == userId) {
                        actionButton.setDisable(false);
                        actionButton.setText("THANH TOÁN");
                        actionButton.setStyle(
                                "-fx-background-color: linear-gradient(to right, -sf-danger, -sf-warning);" +
                                        "-fx-text-fill: -sf-on-accent;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 25;" +
                                        "-fx-cursor: hand;"
                        );
                    } else {
                        actionButton.setDisable(true);
                        actionButton.setText("ĐÃ KẾT THÚC");
                        actionButton.setStyle(
                                "-fx-background-color: -sf-neutral;" +
                                        "-fx-text-fill: -sf-on-accent;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 25;"
                        );
                    }
                }

                LocalDateTime deadline = auction.getEndingTime() != null
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
                            "-fx-background-color: -sf-neutral;" +
                                    "-fx-text-fill: -sf-on-accent;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 25;"
                    );
                }
                break;

            case PAID:
                hideCountdown();
                lbCurrentPrice.setText(String.format("%,.0f VND", auction.getWinningPrice()));
                lbCategory.setText("Loại: " + (
                        auction.getItem().getCategory() != null
                                ? auction.getItem().getCategory()
                                : "Khác"
                ));
                lbWinnerName.setText("Người thắng: " + auction.getWinnerId());

                if (actionButton != null) {
                    actionButton.setDisable(true);
                    actionButton.setText("ĐÃ THANH TOÁN");
                    actionButton.setStyle(
                            "-fx-background-color: -sf-success;" +
                                    "-fx-text-fill: -sf-on-accent;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 25;"
                    );
                }
                break;

            default:
                hideCountdown();
                break;
        }
    }

    @Override
    public void onResponse(Response rp) {
        NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);

        if (!rp.isSuccess()) {
            Platform.runLater(() -> {
                if (auction != null) {
                    auction.setStatus(AuctionStatus.CLOSED);
                }
                showAlert("Thanh toán không thành công", "Vui lòng thử lại");
            });
        }
    }
}
