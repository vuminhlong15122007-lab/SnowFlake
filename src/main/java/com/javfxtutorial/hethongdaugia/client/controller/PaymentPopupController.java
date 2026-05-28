package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PaymentPopupController {

  private static final Preferences PREFS =
          Preferences.userNodeForPackage(PaymentPopupController.class);
  private static final String LAWSUIT_SHOWN_PREFIX = "lawsuit_shown_auction_";

  /**
   * Kiểm tra cảnh báo kiện của phiên này đã từng hiện chưa.
   * Gọi trước khi load FXML để tránh mở popup không cần thiết.
   */
  public static boolean isLawsuitAlreadyShown(long auctionId) {
    return PREFS.getBoolean(LAWSUIT_SHOWN_PREFIX + auctionId, false);
  }

  @FXML private ImageView productImageView;
  @FXML private Label lbProductName;
  @FXML private Label lbWinningPrice;
  @FXML private Label lbAuctionId;
  @FXML private Label lbTime;
  @FXML private VBox paymentSection;
  @FXML private VBox lawsuitSection;
  private Runnable onConfirmed;

  public void setAuction(Auction auction) {
    if (auction == null || auction.getItem() == null) return;

    if (lbProductName != null) lbProductName.setText(auction.getItem().getName());

    if (lbWinningPrice != null) {
      double price =
              auction.getCurrentPrice() != null ? auction.getCurrentPrice().doubleValue() : 0;
      lbWinningPrice.setText(String.format("%,.0f VND", price));
    }

    if (lbAuctionId != null) lbAuctionId.setText("Mã phiên: " + auction.getAuctionId());

    if (productImageView != null
            && auction.getItem().getImage() != null
            && !auction.getItem().getImage().isBlank()) {
      ImageHelper.loadBase64ToImageView(productImageView, auction.getItem().getImage());
    }

    if (lbTime != null && auction.getEndingTime() != null) {
      LocalDateTime deadline = auction.getEndingTime().plusHours(24);
      String prefKey = LAWSUIT_SHOWN_PREFIX + auction.getAuctionId();

      if (LocalDateTime.now().isAfter(deadline)) {
        // Quá 24h, lần đầu vào đây → hiện cảnh báo và đánh dấu đã hiện
        PREFS.putBoolean(prefKey, true);
        showLawsuitWarning();
      } else {
        // Còn thời gian → chạy đồng hồ bình thường
        TimeLeft timer = new TimeLeft(lbTime, deadline);
        timer.setOnFinished(() -> Platform.runLater(() -> {
          PREFS.putBoolean(prefKey, true);
          showLawsuitWarning();
        }));
        timer.start();
      }
    }
  }

  private void showLawsuitWarning() {
    if (paymentSection != null) {
      paymentSection.setVisible(false);
      paymentSection.setManaged(false);
    }
    if (lawsuitSection != null) {
      lawsuitSection.setVisible(true);
      lawsuitSection.setManaged(true);
    }
  }

  @FXML
  public void closePopup() {
    Stage stage = (Stage) lbProductName.getScene().getWindow();
    stage.close();
  }

  public void setOnConfirmed(Runnable callback) {
    this.onConfirmed = callback;
  }

  @FXML
  public void confirmPayment() {
    if (onConfirmed != null) onConfirmed.run();
    closePopup();
  }
}