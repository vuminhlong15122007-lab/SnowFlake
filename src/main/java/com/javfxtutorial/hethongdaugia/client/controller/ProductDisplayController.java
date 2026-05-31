package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.Util.AuctionModificationManager;
import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;
import com.javfxtutorial.hethongdaugia.client.Util.ToastNotifier;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.*;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDisplayController implements ResponseListener {

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
  @FXML private Label lbLoaisp;
  @FXML private VBox artInfoBox, vehicleInfoBox, electronicsInfoBox;
  @FXML private Label artTitleValue, artistValue, yearCreatedValue;
  @FXML private Label licensePlateValue, vehicleYearValue, brandValue, colorValue;
  @FXML private Label elecBrandValue, modelValue;
  @FXML private Label initPriceLabel, stepPriceLabel;
  @FXML private Label detailTitle;
  @FXML private NotificationToastController notificationToastController;

  private Item item;
  private Auction auction;
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
  private static final Logger log = LoggerFactory.getLogger(ProductDisplayController.class);

  private final NetworkManager networkManager = NetworkManager.getInstance();
  private TimeLeft timer;

  public void setData(Auction auction) {
    this.auction = auction;
    this.item = auction.getItem();
    LbMotasp.setText(item.getDescription());
    StartTimeLabel.setText(auction.getStartingTime().format(TIME_FMT));
    EndingtimeLabel.setText(auction.getEndingTime().format(TIME_FMT));
    lbTenngban.setText(auction.getItem().getSellerName());
    ItemNameLabel.setText(item.getName());
    ItemPriceLabel.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
    stepPriceLabel.setText(String.format("%,.0f VND", auction.getStepPrice()));
    initPriceLabel.setText(String.format("%,.0f VND", auction.getInitPrice()));
    ItemCategory category = item.getCategory();
    lbLoaisp.setText(category != null ? category.name() : "Không xác định");
    ImageHelper.loadBase64ToImageView(itemImageView, auction.getItem().getImage());
    auction
        .statusProperty()
        .addListener(
            ((_, _, newVal) -> {
              updateUI(newVal);
            })); // thay đổi UI nếu có status mơid
  }

  @FXML
  public void initialize() {
    auction = ClientModel.getInstance().getCurrentAuction();
    item = auction.getItem();
    NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
    NetworkManager.getInstance().register(UpdateAuctionCommand.class, this);
    setData(auction);
    showCategoryInfo();
    updateUI(auction.getStatus());
  }

  private void updateUI(AuctionStatus status) {
    switch (status) {
      case RUNNING -> {
        UI01.setText("THỜI GIAN CÒN LẠI");
        UI01.setStyle("-fx-text-fill: -sf-success; -fx-alignment: CENTER;"); // ← đổi màu xanh
        UI02.setStyle(
            "-fx-background-color: -sf-surface; -fx-background-radius: 10; "
                + "-fx-border-radius: 10; -fx-border-color: -sf-success; -fx-alignment: CENTER;"); // ← viền xanh
        lbtimeLeft.setStyle("-fx-text-fill: -sf-success;");
        ThamGiaDauGiaBtn.setText("Tham gia");
        ThamGiaDauGiaBtn.setStyle("");
        if (timer != null) timer.stop();
        timer = new TimeLeft(lbtimeLeft, auction.getEndingTime());
        timer.setOnFinished(
            () -> {
              auction.setStatus(AuctionStatus.CLOSED);
              Platform.runLater(
                  () -> {
                    try {
                      ServerConnection connection = NetworkManager.getConnection();
                      Command cmd = new UpdateAuctionStatusCommand(auction);
                      connection.sendCommand(cmd);
                    } catch (ConnectionFailedException e) {
                      log.error("Không kết nối được server");
                      notifyError(e.getMessage());
                    } catch (SendFailedException e) {
                      log.error("Không gửi được command");
                      notifyError(e.getMessage());
                    }
                  });
            });
        timer.start();
      }
      case NOT_START -> {
        UI01.setStyle("-fx-text-fill: -sf-warning; -fx-alignment: CENTER;");
        UI02.setStyle(
            "-fx-background-color: -sf-surface; -fx-background-radius: 10; "
                + "-fx-border-radius: 10; -fx-border-color: -sf-warning; -fx-alignment: CENTER;");
        lbtimeLeft.setStyle("-fx-text-fill: -sf-warning;");
        ThamGiaDauGiaBtn.setText("Chưa thể tham gia");
        UI01.setText("THỜI GIAN CÒN LẠI ĐỂ BẮT ĐẦU");
        ThamGiaDauGiaBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, -sf-danger, -sf-warning); "
                + "-fx-text-fill: -sf-on-accent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25;");
        if (timer != null) timer.stop();
        timer = new TimeLeft(lbtimeLeft, auction.getStartingTime());
        timer.setOnFinished(
            () -> {
              auction.setStatus(AuctionStatus.RUNNING);
              Platform.runLater(
                  () -> {
                    try {
                      ServerConnection connection = NetworkManager.getConnection();
                      Command cmd = new UpdateAuctionStatusCommand(auction);
                      connection.sendCommand(cmd);
                    } catch (ConnectionFailedException e) {
                      log.error("Không kết nối được server");
                      notifyError(e.getMessage());
                    } catch (SendFailedException e) {
                      log.error("Không gửi được command");
                      notifyError(e.getMessage());
                    }
                  });
            });
        timer.start();
      }
      default -> { // CLOSED
        lbtimeLeft.setText("ĐÃ KẾT THÚC");
        UI01.setStyle("-fx-text-fill: -sf-danger; -fx-alignment: CENTER;");
        UI02.setStyle(
            "-fx-background-color: -sf-surface; -fx-background-radius: 10; "
                + "-fx-border-radius: 10; -fx-border-color: -sf-danger; -fx-alignment: CENTER;");
        lbtimeLeft.setStyle("-fx-text-fill: -sf-danger;");
        ThamGiaDauGiaBtn.setText("Phiên đấu giá đã đóng");
        ThamGiaDauGiaBtn.setStyle(
            "-fx-background-color: -sf-neutral; -fx-text-fill: -sf-on-accent; "
                + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25;");
      }
    }
  }

  @FXML
  public void QuaylaiMenu(ActionEvent event) {
    NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);
    NetworkManager.getInstance().unregister(UpdateAuctionCommand.class, this);
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
  }

  @FXML
  public void goToManHinhDauGiaTrucTiep(ActionEvent event) {
    AccountType type = ClientModel.getInstance().getCurrentUser().getAccountType();
    if (type == AccountType.ADMIN) {
      notifyWarning("Admin không thể tham gia phiên đấu giá");
      return;
    }
    if (auction.getStatus() == AuctionStatus.RUNNING) {
      changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/LiveAuction.fxml");
    } else {
      if (auction.getStatus() == AuctionStatus.CLOSED) {
        notifyWarning("Đã hết phiên đấu giá");
      } else if (auction.getStatus() == AuctionStatus.NOT_START) {
        notifyWarning("Chưa bắt đầu phiên đấu giá");
      }
    }
  }

  private void showCategoryInfo() {
    hideBox(artInfoBox);
    hideBox(vehicleInfoBox);
    hideBox(electronicsInfoBox);
    if (item == null) return;

    if (item instanceof Art art) {
      showBox(artInfoBox);
      if (detailTitle != null) detailTitle.setText("THÔNG TIN ART");
      if (artTitleValue != null)
        artTitleValue.setText(art.getTitle() != null ? art.getTitle() : "...");
      if (artistValue != null)
        artistValue.setText(art.getArtist() != null ? art.getArtist() : "...");
      if (yearCreatedValue != null) yearCreatedValue.setText(String.valueOf(art.getYearCreated()));
    } else if (item instanceof Vehicle vehicle) {
      showBox(vehicleInfoBox);
      if (detailTitle != null) detailTitle.setText("THÔNG TIN VEHICLE");
      if (licensePlateValue != null)
        licensePlateValue.setText(
            vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "...");
      if (vehicleYearValue != null)
        vehicleYearValue.setText(vehicle.getYear() > 0 ? String.valueOf(vehicle.getYear()) : "...");
      if (brandValue != null)
        brandValue.setText(vehicle.getBrand() != null ? vehicle.getBrand() : "...");
      if (colorValue != null)
        colorValue.setText(vehicle.getColor() != null ? vehicle.getColor() : "...");
    } else if (item instanceof Electronics elec) {
      showBox(electronicsInfoBox);
      if (detailTitle != null) detailTitle.setText("THÔNG TIN ELECTRONICS");
      if (elecBrandValue != null)
        elecBrandValue.setText(elec.getBrand() != null ? elec.getBrand() : "...");
      if (modelValue != null) modelValue.setText(elec.getModel() != null ? elec.getModel() : "...");
    }
  }

  private void hideBox(VBox box) {
    if (box != null) {
      box.setVisible(false);
      box.setManaged(false);
    }
  }

  private void showBox(VBox box) {
    if (box != null) {
      box.setVisible(true);
      box.setManaged(true);
    }
  }

  @Override
  public void onResponse(Response rp) {
    if (rp.getCommand().getClass().equals(UpdateAuctionCommand.class)
        || rp.getCommand().getClass().equals(UpdateAuctionStatusCommand.class)) {
      if (rp.isSuccess()) {
        auction = (Auction) rp.getPayLoad();
        Platform.runLater(
            () -> {
              setData(auction);
              showCategoryInfo();
              if (auction.getStatus().equals(AuctionStatus.NOT_START)) {
                if (timer != null) timer.setDeadline(auction.getStartingTime());
              } else if (auction.getStatus().equals(AuctionStatus.RUNNING)) {
                if (timer != null) timer.setDeadline(auction.getEndingTime());
              }
            });
      }
    }
    if (rp.getCommand().getClass().equals(UpdateAuctionStatusCommand.class)) {
      if ("ADMIN_CANCELLED_AUCTION".equals(rp.getMessage())) {
        Platform.runLater(
            () -> {
              if (timer != null) timer.stop();
              Scene scene = lbtimeLeft.getScene();
              if (scene == null || scene.getWindow() == null) return;
              Stage stage = (Stage) scene.getWindow();
              showAlert("Thông báo", "Phiên đấu giá đã bị admin hủy.");
              try {
                Parent root =
                    FXMLLoader.load(
                        getClass()
                            .getResource(
                                "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml"));
                stage.setScene(new Scene(root));
              } catch (Exception e) {
                log.error("Lỗi chuyển scene: {}", e.getMessage());
              }
            });
      }
    }
  }

  private void notifyWarning(String message) {
    toast().warning(message);
  }

  private void notifyError(String message) {
    toast().error(message);
  }

  private ToastNotifier toast() {
    return ToastNotifier.of(notificationToastController);
  }
}
