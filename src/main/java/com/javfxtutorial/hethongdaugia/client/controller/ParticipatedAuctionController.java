package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetParticipatedAuctionsByBidderCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipatedAuctionController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(ParticipatedAuctionController.class);
  @FXML private Button btnAll;
  @FXML private Button btnCTToan;
  @FXML private Button btnDTGia;
  @FXML private Button btnDTToan;
  @FXML private Button btnDaHuy;
  @FXML private Button btnDaThamGia;
  @FXML private ListView<Auction> productList;
  @FXML private TextField searchField;
  @FXML private Label sectionTitle;

  private final ObservableList<Auction> participatedAuctionList =
      FXCollections.observableArrayList();
  private FilteredList<Auction> filterData;
  private AuctionStatus currentStatus = null;
  private boolean filterLoser = false;

  @FXML
  void goMenu(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
  }

  @FXML
  public void initialize() {
    VBox.setVgrow(productList, Priority.ALWAYS);
    productList.setMaxWidth(Double.MAX_VALUE);
    productList.setCellFactory(_ -> new ParticipatedAuctionCell());
    // Tạo FilteredList
    filterData = new FilteredList<>(participatedAuctionList, _ -> true);
    productList.setItems(filterData);
    // Tìm kiếm
    searchField.textProperty().addListener((_, _, _) -> applyFilters());

    // Nút lọc trạng thái
    btnAll.setOnAction(
        _ -> {
          currentStatus = null;
          sectionTitle.setText("📋  " + "TẤT CẢ PHIÊN ĐẤU GIÁ ĐÃ THAM GIA");
          filterLoser = false;
          setActiveButton(btnAll);
          applyFilters();
        });
    btnDTToan.setOnAction(
        _ -> {
          currentStatus = AuctionStatus.PAID;
          sectionTitle.setText("📋  " + "PHIÊN ĐẤU GIÁ ĐÃ THANH TOÁN");
          filterLoser = false;
          setActiveButton(btnDTToan);
          applyFilters();
        });
    btnCTToan.setOnAction(
        _ -> {
          currentStatus = AuctionStatus.CLOSED;
          filterLoser = false;
          sectionTitle.setText("📋  " + "PHIÊN ĐẤU GIÁ CHƯA THANH TOÁN");
          setActiveButton(btnCTToan);
          applyFilters();
        });
    btnDTGia.setOnAction(
        _ -> {
          currentStatus = AuctionStatus.RUNNING;
          sectionTitle.setText("📋  " + "PHIÊN ĐẤU GIÁ ĐANG THAM GIA ");
          filterLoser = false;
          setActiveButton(btnDTGia);
          applyFilters();
        });
    btnDaHuy.setOnAction(
        e -> {
          currentStatus = AuctionStatus.CANCELLED;
          sectionTitle.setText("📋  PHIÊN ĐẤU GIÁ ĐÃ BỊ HỦY");
          filterLoser = false;
          setActiveButton(btnDaHuy);
          applyFilters();
        });
    btnDaThamGia.setOnAction(
        e -> {
          currentStatus = AuctionStatus.CLOSED;
          filterLoser = true;
          sectionTitle.setText("📋  PHIÊN ĐẤU GIÁ ĐÃ THAM GIA");
          setActiveButton(btnDaThamGia);
          applyFilters();
        });
    setActiveButton(btnAll);
    loadData();
  }

  // Hàm áp dụng tất cả bộ lọc
  private void applyFilters() {
    filterData.setPredicate(
        auction -> {
          if (auction == null || auction.getItem() == null) return false;

          // 1. Tìm kiếm theo tên
          String search = searchField.getText();
          if (search != null && !search.isBlank()) {
            String name = auction.getItem().getName();
            if (name == null || !name.toLowerCase().contains(search.toLowerCase())) {
              return false;
            }
          }

          // 2. Lọc theo trạng thái
          if (currentStatus == AuctionStatus.CANCELLED) {
            return auction.getStatus() == AuctionStatus.CANCELLED_BY_ADMIN
                || auction.getStatus() == AuctionStatus.CANCELLED;
          }
          if (currentStatus != null && auction.getStatus() != currentStatus) {
            return false;
          }
          if (currentStatus == AuctionStatus.CLOSED || currentStatus == AuctionStatus.PAID) {
            String userName = ClientModel.getInstance().getCurrentUser().getName();
            if (filterLoser) { // Đã tham gia nhưng không thắng
              return !auction.getWinnerName().equals(userName);
            } else { // Chờ thanh toán / đã thanh toán → user thắng
              return auction.getWinnerName().equals(userName);
            }
          }

          return true;
        });
  }

  // Đổi nút đang active bằng CSS class để không phá theme khi đổi màu
  private void setActiveButton(Button active) {
    Button[] buttons = {btnAll, btnCTToan, btnDTGia, btnDTToan, btnDaHuy, btnDaThamGia};
    for (Button b : buttons) {
      if (b == null) continue;
      b.setStyle("");
      b.getStyleClass().removeAll("sf-filter-button", "sf-filter-button-active");
      b.getStyleClass().add(b == active ? "sf-filter-button-active" : "sf-filter-button");
    }
  }

  public void loadData() {
    Command cmd = new GetParticipatedAuctionsByBidderCommand();
    cmd.addData("currentUserId", ClientModel.getInstance().getCurrentUser().getId());
    new Thread(
            () -> {
              try {
                NetworkManager.getInstance().sendRequest(cmd, this);
              } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                NetworkManager.getInstance()
                    .unregister(GetParticipatedAuctionsByBidderCommand.class, this);
                Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu", "Loading.gif"));
              } catch (Exception e) {
                log.error("Lỗi load data: {}", e.getMessage(), e);
                NetworkManager.getInstance()
                    .unregister(GetParticipatedAuctionsByBidderCommand.class, this);
                Platform.runLater(() -> showAlert("Lỗi", "Tải dữ liệu thất bại", "Loading.gif"));
              }
            })
        .start();
  }

  @Override
  public void onResponse(Response rp) {
    if (rp.getCommand().getClass() == GetParticipatedAuctionsByBidderCommand.class) {
      Platform.runLater(
          () -> {
            if (!rp.isSuccess()) {
              showAlert("Loi tai du lieu", "Không thể tải dữ liệu", "Loading.gif");
              return;
            }
            if (rp.getPayLoad() == null) {
              return;
            }
            ArrayList<Auction> auctions = (ArrayList<Auction>) rp.getPayLoad();
            participatedAuctionList.setAll(auctions);
          });
      NetworkManager networkManager = NetworkManager.getInstance();
      networkManager.unregister(GetParticipatedAuctionsByBidderCommand.class, this);
    }
  }
}
