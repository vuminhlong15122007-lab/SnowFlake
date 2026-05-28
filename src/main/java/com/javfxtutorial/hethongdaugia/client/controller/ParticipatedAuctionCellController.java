package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.time.LocalDateTime;
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

public class ParticipatedAuctionCellController implements ResponseListener {
  private static final Logger log =
          LoggerFactory.getLogger(ParticipatedAuctionCellController.class);
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
      timer.setOnFinished(
              () -> {
                // Khi hết giờ, đổi badge sang đỏ
                if (lbTime != null) {
                  lbTime.setStyle(
                          "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -sf-danger;");
                }
                if (auction.getStatus()
                        == AuctionStatus.RUNNING) { // hết countdown running → chuyển CLOSED
                  auction.setStatus(AuctionStatus.CLOSED);
                  Platform.runLater(
                          () -> {
                            try {
                              Command cmd = new UpdateAuctionStatusCommand(auction);
                              NetworkManager.getInstance().sendRequest(cmd, this);
                            } catch (ConnectionFailedException e) {
                              log.error("Không kết nối được server");
                              showAlert("Lỗi", e.getMessage());
                            } catch (SendFailedException e) {
                              log.error("Không gửi được command");
                              showAlert("Lỗi", e.getMessage());
                            }
                          });
                } else if (auction.getStatus()
                        == AuctionStatus.CLOSED) { // hết countdown chờ thanh toán → CANCELLED
                  auction.setStatus(AuctionStatus.CANCELLED);
                  Platform.runLater(
                          () -> {
                            try {
                              Command cmd = new UpdateAuctionStatusCommand(auction);
                              NetworkManager.getInstance().sendRequest(cmd, this);
                            } catch (ConnectionFailedException e) {
                              log.error("Không kết nối được server");
                              showAlert("Lỗi", e.getMessage());
                            } catch (SendFailedException e) {
                              log.error("Không gửi được command");
                              showAlert("Lỗi", e.getMessage());
                            }
                          });
                }
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
    updateUI(auction.getStatus()); // khởi tạo UI ban đầu
    auction
            .statusProperty()
            .addListener(
                    ((_, _, newVal) -> {
                      updateUI(newVal);
                    })); // thay đổi UI nếu có status mơid
    lbProductName.setText(auction.getItem().getName());
    lbCategory.setText(String.valueOf(auction.getItem().getCategory()));
    lbWinnerName.setText(String.valueOf(auction.getWinnerName()));
    if (auction.getWinningPrice() != null && auction.getWinningPrice().doubleValue() > 0) {
      lbCurrentPrice.setText(String.format("%,.0f VND", auction.getWinningPrice()));
    } else {
      lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
    }

    if (productImage != null
            && auction.getItem().getImage() != null
            && !auction.getItem().getImage().isBlank()) {
      ImageHelper.loadBase64ToImageView(productImage, auction.getItem().getImage());
    }
  }

  @FXML
  public void goPayment(ActionEvent event) {
    if (auction == null) return;
    AuctionStatus status = auction.getStatus();

    if (status == AuctionStatus.CLOSED) {
      // Nếu cảnh báo kiện đã hiện rồi → không mở popup nữa
      if (PaymentPopupController.isLawsuitAlreadyShown(auction.getAuctionId())) return;
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
      FXMLLoader loader =
              new FXMLLoader(
                      getClass()
                              .getResource("/com/javfxtutorial/hethongdaugia/view/fxml/PaymentPopup.fxml"));
      Parent root = loader.load();
      PaymentPopupController popupController = loader.getController();
      popupController.setAuction(auction);

      popupController.setOnConfirmed(
              () -> {
                auction.setStatus(AuctionStatus.PAID);
                try {
                  NetworkManager.getInstance()
                          .sendRequest(new UpdateAuctionStatusCommand(auction), this);
                } catch (SendFailedException | ConnectionFailedException e) {
                  Platform.runLater(
                          () -> showAlert("Lỗi", "Không thể gửi yêu cầu thanh toán.", "Wrong.gif"));
                }
              });

      Stage popupStage = new Stage();
      popupStage.initModality(Modality.APPLICATION_MODAL);
      popupStage.setScene(new Scene(root));
      popupStage.show();
    } catch (Exception e) {
      log.error("Không thể mở cửa sổ thanh toán: {}", e.getMessage(), e);
      showAlert("Lỗi", "Không thể mở cửa sổ thanh toán: " + e.getMessage());
    }
  }

  private void updateUI(AuctionStatus status) {
    switch (status) {
      case RUNNING:
        hideCountdown();
        lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbWinnerName.setText(String.valueOf(auction.getWinnerName()));
        if (actionButton != null) {
          actionButton.setDisable(false);
          actionButton.setText("THAM GIA");
          setActionButtonClass("sf-auction-action-primary");
        }
        break;

      case CLOSED:
        lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbWinnerName.setText("Người thắng: " + auction.getWinnerName());
        String userName = ClientModel.getInstance().getCurrentUser().getName();
        if (auction.getWinnerName().equals(userName)) {
          actionButton.setDisable(false);
          actionButton.setText("THANH TOÁN");
          setActionButtonClass("sf-auction-action-warning");
          LocalDateTime deadline =
                  (auction.getEndingTime() != null)
                          ? auction.getEndingTime().plusHours(24)
                          : LocalDateTime.now().plusHours(24);
          showCountdown(deadline);
        } else {
          hideCountdown();
          actionButton.setDisable(true);
          actionButton.setText("ĐÃ KẾT THÚC");
          setActionButtonClass("sf-auction-action-neutral");
        }
        break;

      case CANCELLED:
        hideCountdown();
        lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbWinnerName.setText("Phiên bị hủy");
        if (actionButton != null) {
          actionButton.setDisable(true);
          actionButton.setText("ĐÃ HỦY");
          setActionButtonClass("sf-auction-action-danger");
        }
        break;

      case CANCELLED_BY_ADMIN:
        hideCountdown();
        lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbWinnerName.setText("Phiên bị hủy");
        if (actionButton != null) {
          actionButton.setDisable(true);
          actionButton.setText("ĐÃ HỦY");
          setActionButtonClass("sf-auction-action-danger");
        }
        break;

      case PAID:
        hideCountdown();
        lbCurrentPrice.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbCategory.setText(
                "Loại: "
                        + (auction.getItem().getCategory() != null
                        ? auction.getItem().getCategory()
                        : "Khác"));
        lbWinnerName.setText("Người thắng: " + auction.getWinnerName());
        if (actionButton != null) {
          actionButton.setDisable(true);
          actionButton.setText("ĐÃ THANH TOÁN");
          setActionButtonClass("sf-auction-action-success");
        }
        lbTime = null;
        break;

      default:
        hideCountdown();
        break;
    }
  }

  private void setActionButtonClass(String styleClass) {
    if (actionButton == null) return;
    actionButton.setStyle("");
    actionButton
            .getStyleClass()
            .removeAll(
                    "sf-auction-action-primary",
                    "sf-auction-action-warning",
                    "sf-auction-action-neutral",
                    "sf-auction-action-success",
                    "sf-auction-action-danger");
    actionButton.getStyleClass().add(styleClass);
  }

  @Override
  public void onResponse(Response rp) {
    NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);
    if (!rp.isSuccess()) {
      Platform.runLater(
              () -> {
                auction.setStatus(AuctionStatus.CLOSED);
                showAlert("Thanh toán không thành công", "vui lòng thử lại");
              });
    }
  }
}