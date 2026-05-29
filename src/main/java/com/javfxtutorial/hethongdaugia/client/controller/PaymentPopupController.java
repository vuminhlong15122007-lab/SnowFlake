package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PaymentPopupController {

  @FXML private ImageView productImageView;
  @FXML private Label lbProductName;
  @FXML private Label lbWinningPrice;
  @FXML private Label lbAuctionId;
  @FXML private Label lbTime;
  @FXML private VBox lawsuitSection;
  @FXML private VBox paymentSection;
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

    // Đếm ngược 24h kể từ endingTime của phiên
    if (lbTime != null && auction.getEndingTime() != null) {
      LocalDateTime deadline = auction.getEndingTime().plusHours(24);
      TimeLeft timer = new TimeLeft(lbTime, deadline);
      timer.setOnFinished(() -> {
        paymentSection.setVisible(false);
        paymentSection.setManaged(false);
        lawsuitSection.setVisible(true);
        lawsuitSection.setManaged(true);
      });
      timer.start();
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
