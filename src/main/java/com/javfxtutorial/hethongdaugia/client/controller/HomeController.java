package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

import com.javfxtutorial.hethongdaugia.client.Util.AppIcon;
import com.javfxtutorial.hethongdaugia.client.Util.UIUtils;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetUnpaidAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  @FXML
  public void goToProfile(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/UserInformation.fxml");
  }

  @FXML
  public void goAuction(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
  }

  @FXML
  public void goLogin(ActionEvent event) {
    // Xóa toàn bộ dữ liệu phiên cũ — tài khoản mới sẽ bắt đầu sạch
    ClientModel.getInstance().logout();
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
  }

  @FXML
  public void manageProducts(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductManagement.fxml");
  }

  @FXML
  public void goParticipatedAuction(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/UserParticipatedAuction.fxml");
  }

  @Override
  public void onResponse(Response rp) {
    // Chỉ xử lý response của GetUnpaidAuctionCommand
    if (!(rp.getCommand() instanceof GetUnpaidAuctionCommand)) return;
    NetworkManager.getInstance().unregister(GetUnpaidAuctionCommand.class, this);

    if (rp.isSuccess()) {
      ArrayList<Auction> unpaidList = (ArrayList<Auction>) rp.getPayLoad();
      Platform.runLater(
              () -> {
                if (unpaidList != null && !unpaidList.isEmpty()) {
                  showPaymentPopupChain(unpaidList, 0);
                }
              });
    }
  }

  private void showPaymentPopupChain(ArrayList<Auction> unpaidList, int index) {
    if (index >= unpaidList.size()) return;

    Auction auction = unpaidList.get(index);
    try {
      FXMLLoader loader =
              new FXMLLoader(
                      UIUtils.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/PaymentPopup.fxml"));
      Parent root = loader.load();
      PaymentPopupController ctrl = loader.getController();
      ctrl.setAuction(auction);

      Stage popup = new Stage();
      AppIcon.apply(popup);
      popup.setScene(new Scene(root));

      ctrl.setOnConfirmed(
              () -> {
                markAsPaid(auction);
                popup.close();
                showPaymentPopupChain(unpaidList, index + 1);
              });

      popup.show();
    } catch (IOException e) {
      log.error("Lỗi load PaymentPopup: {}", e.getMessage(), e);
      showPaymentPopupChain(unpaidList, index + 1);
    }
  }

  private void markAsPaid(Auction auction) {
    new Thread(
            () -> {
              try {
                auction.setStatus(AuctionStatus.PAID);
                UpdateAuctionStatusCommand cmd = new UpdateAuctionStatusCommand(auction);
                NetworkManager.getConnection().sendCommand(cmd);
              } catch (Exception e) {
                log.error("Lỗi mark PAID: {}", e.getMessage());
              }
            })
            .start();
  }

  public void checkUnpaidAuction() {
    String userNmame = ClientModel.getInstance().getCurrentUser().getName();
    new Thread(
            () -> {
              try {
                GetUnpaidAuctionCommand cmd = new GetUnpaidAuctionCommand(userNmame);
                NetworkManager.getInstance().sendRequest(cmd, this);
              } catch (Exception e) {
                log.error("Lỗi check unpaid: {}", e.getMessage());
              }
            })
            .start();
  }
}