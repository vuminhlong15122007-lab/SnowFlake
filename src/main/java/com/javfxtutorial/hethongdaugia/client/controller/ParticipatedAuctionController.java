package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class ParticipatedAuctionController implements  ResponseListener {
  @FXML private Button btnAll;
  @FXML private Button btnCTToan;
  @FXML private Button btnDTGia;
  @FXML private Button btnDTToan;
  @FXML private Button goMenu;
  @FXML private ListView<Auction> productList;
  @FXML private TextField searchField;
  @FXML private Label sectionTitle;

  private ObservableList<Auction> participatedAuctionList = FXCollections.observableArrayList();
  private FilteredList<Auction> filterData;
  private AuctionStatus currentStatus = null;

  @FXML void goMenu(ActionEvent event) { changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");}

  @FXML
  public void initialize() {
    VBox.setVgrow(productList, Priority.ALWAYS);
    productList.setMaxWidth(Double.MAX_VALUE);
    productList.setCellFactory(lv -> new ParticipatedAuctionCell());
    // Tạo FilteredList
    filterData = new FilteredList<>(participatedAuctionList, auction -> true);
    productList.setItems(filterData);
    // ========== THÊM: Tìm kiếm ==========
    searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());


    // ========== THÊM: Nút lọc trạng thái ==========
    btnAll.setOnAction(e -> {
      currentStatus = null;
      setActiveButton(btnAll);
      applyFilters();
    });
    btnDTToan.setOnAction(e -> {
      currentStatus = AuctionStatus.NOT_START;
      sectionTitle.setText("📋  " + "PHIÊN ĐẤU GIÁ ĐÃ THANH TOÁN");
      setActiveButton(btnDTToan);
      applyFilters();
    });
    btnCTToan.setOnAction(e -> {
      currentStatus = AuctionStatus.RUNNING;
      sectionTitle.setText("📋  " + "PHIÊN ĐẤU GIÁ CHƯA THANH TOÁN");
      setActiveButton(btnCTToan);
      applyFilters();
    });
    btnDTGia.setOnAction(e -> {
      currentStatus = AuctionStatus.CLOSED;
      sectionTitle.setText("📋  " + "PHIÊN ĐẤU GIÁ ĐÃ THAM GIA ");
      setActiveButton(btnDTGia);
      applyFilters();
    });

    loadData();
  }

  // ========== THÊM: Hàm áp dụng tất cả bộ lọc ==========
  private void applyFilters() {
    filterData.setPredicate(auction -> {
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
      if (currentStatus != null && auction.getStatus() != currentStatus) {
        return false;
      }

      return true;
    });
  }





  // ========== THÊM: Đổi màu nút đang active ==========
  private void setActiveButton(Button active) {
    Button[] buttons = {btnAll, btnCTToan, btnDTGia, btnDTToan};
    String activeStyle = "-fx-background-color: linear-gradient(to right, #56ccf2, #2f80ed); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 15;";
    String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 15; -fx-border-color: #dcdde1; -fx-border-radius: 8;";
    for (Button b : buttons) {
      b.setStyle(b == active ? activeStyle : inactiveStyle);
    }
  }

  // NHỚ THÊM COMMAND LẤY RA TRẠNG THÁI ĐÃ THANH TOÁN....
  public void loadData() {
    Command cmd = new GetAllAuctionsCommand();
    ServerConnection connection = NetworkManager.getConnection();
    new Thread(() -> {
      connection.sendCommand(cmd);
      NetworkManager networkManager = NetworkManager.getInstance();
      networkManager.register(GetAllAuctionsCommand.class, this);
    }).start();
  }

  private void getParticipatedAuctionList(){

  }

  @Override
  public void onResponse(Response rp) {
    if (rp.getCommand().getClass() == GetAllAuctionsCommand.class) {
      Platform.runLater(() -> {
        if (rp == null) {
          showAlert("Loi tai du lieu", "Server khong tra ve du lieu phien dau gia.", "Loading.gif");
          return;
        }

        if (!rp.isSuccess()) {
          showAlert("Loi tai du lieu", rp.getMessage(), "Loading.gif");
          return;
        }

        ArrayList<Auction> auctions = (ArrayList<Auction>) rp.getPayLoad();
        participatedAuctionList.setAll(auctions);
      });
      NetworkManager networkManager = NetworkManager.getInstance();
      networkManager.unregister(GetAllAuctionsCommand.class, this);
    }
  }


}
