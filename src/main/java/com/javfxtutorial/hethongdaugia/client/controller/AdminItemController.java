package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

import com.javfxtutorial.hethongdaugia.client.Util.ToastNotifier;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminItemController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(AdminItemController.class);

  @FXML private TableView<Auction> itemTable;
  @FXML private TableColumn<Auction, Integer> colId;
  @FXML private TableColumn<Auction, String> colItemName;
  @FXML private TableColumn<Auction, String> colStartPrice;
  @FXML private TableColumn<Auction, String> colStepPrice;
  @FXML private TableColumn<Auction, String> colCategory;
  @FXML private TableColumn<Auction, String> colOwner;
  @FXML private TableColumn<Auction, String> colStatus;
  @FXML private TextField searchField;

  /** Badge "X sản phẩm" trên header bảng — inject từ FXML */
  @FXML private Label itemCountBadge;

  @FXML private NotificationToastController notificationToastController;

  private ObservableList<Auction> observableList;

  // ─────────────────────────────────────────────────────────────
  //  Helper: cập nhật badge đếm số dòng đang hiển thị
  // ─────────────────────────────────────────────────────────────

  /**
   * Cập nhật badge theo trạng thái hiện tại của bảng. - Hiển thị đầy đủ → "25 sản phẩm" - Đang
   * search/lọc → "3 / 25 sản phẩm"
   */
  private void updateCountBadge() {
    if (itemCountBadge == null) return;
    int total = (observableList != null) ? observableList.size() : 0;
    int showing = itemTable.getItems().size();

    if (showing == total) {
      itemCountBadge.setText(total + " sản phẩm");
    } else {
      itemCountBadge.setText(showing + " / " + total + " sản phẩm");
    }
  }

  @FXML
  public void initialize() {
    observableList = ClientModel.getInstance().getAllAuctions();
    itemTable.setItems(observableList);
    colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
    colItemName.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
    colOwner.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
    colStartPrice.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                String.format("%,.0f VND", cellData.getValue().getCurrentPrice())));
    colStepPrice.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                String.format("%,.0f VND", cellData.getValue().getStepPrice())));
    colCategory.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getItem().getCategory().name()));
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

    // Badge mặc định khi chưa tải xong
    if (itemCountBadge != null) {
      itemCountBadge.setText("Đang tải...");
    }

    // Nếu dữ liệu đã có sẵn trong ClientModel thì cập nhật badge ngay
    if (observableList != null && !observableList.isEmpty()) {
      updateCountBadge();
    }

    try {
      if (!ClientModel.getInstance().isAllAuctionsLoaded) {
        loadItemData();
      }
    } catch (IOException
        | ClassNotFoundException
        | SendFailedException
        | ConnectionFailedException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadItemData()
      throws IOException, ClassNotFoundException, SendFailedException, ConnectionFailedException {
    Command cmd = new GetAllAuctionsCommand();
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.sendRequest(cmd, this);
    log.trace("Gửi yêu cầu load allAuctions");
  }

  public void clickButtonExit(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
  }

  public void clickToGoUserAdmin(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_UserManagement.fxml");
  }

  @FXML
  public void clickToGoAuction(ActionEvent event) {
    Auction selected = itemTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      toast().warning("Vui lòng chọn một sản phẩm.");
      return;
    }
    ClientModel.getInstance().setCurrentAuction(selected);
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/LiveAuction.fxml");
  }

  @FXML
  public void clickToCancelAuction(ActionEvent event) {
    Auction selected = itemTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      toast().warning("Vui lòng chọn một phiên đấu giá.");
      return;
    }
    if (selected.getStatus().equals(AuctionStatus.RUNNING)
        || selected.getStatus().equals(AuctionStatus.NOT_START)) {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Xác nhận hủy");
      confirm.setHeaderText("Hủy phiên đấu giá?");
      confirm.setContentText("Sản phẩm: " + selected.getItem().getName());
      ButtonType yes = new ButtonType("Hủy phiên", ButtonBar.ButtonData.YES);
      ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
      confirm.getButtonTypes().setAll(yes, no);

      confirm
          .showAndWait()
          .ifPresent(
              result -> {
                if (result == yes) {
                  try {
                    selected.setStatus(AuctionStatus.CANCELLED_BY_ADMIN);
                    UpdateAuctionStatusCommand cmd = new UpdateAuctionStatusCommand(selected);
                    NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
                    NetworkManager.getConnection().sendCommand(cmd);
                  } catch (Exception e) {
                    toast().error("Không thể hủy phiên: " + e.getMessage());
                  }
                }
              });
    } else {
      toast().info("Phiên này đã kết thúc hoặc đã bị hủy.");
    }
  }

  @FXML
  public void clickToSearch() {
    String textWord = searchField.getText();

    if (textWord == null || textWord.trim().isEmpty()) {
      itemTable.setItems(observableList);
      updateCountBadge(); // hiển thị tổng đầy đủ
      return;
    }

    ObservableList<Auction> result = FXCollections.observableArrayList();
    String keyword = textWord.toLowerCase().trim();

    for (Auction auction : observableList) {
      if (String.valueOf(auction.getAuctionId()).contains(keyword)
          || auction.getItem().getName().toLowerCase().contains(keyword)
          || auction.getItem().getSellerName().toLowerCase().contains(keyword)
          || auction.getStatus().name().toLowerCase().contains(keyword)) {
        result.add(auction);
      }
    }

    itemTable.setItems(result);
    updateCountBadge(); // hiển thị "X / Y sản phẩm"
  }

  public void clickToDeleteSearch() {
    searchField.clear();
    itemTable.setItems(observableList);
    updateCountBadge(); // về lại tổng đầy đủ
  }

  public void reLoad()
      throws SendFailedException, IOException, ClassNotFoundException, ConnectionFailedException {
    if (itemCountBadge != null) itemCountBadge.setText("Đang tải...");
    loadItemData();
    System.out.println("Dữ liệu đã được cập nhật!");
  }

  @Override
  public void onResponse(Response rp) {
    // ── Cập nhật trạng thái phiên (hủy) ──
    if (rp.getCommand().getClass() == UpdateAuctionStatusCommand.class) {
      if ("ADMIN_CANCELLED_AUCTION".equals(rp.getMessage())) return;

      NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);
      Platform.runLater(
          () -> {
            if (rp.isSuccess()) {
              toast().success("Đã hủy phiên đấu giá.");
              try {
                Object payload = rp.getPayLoad();
                if (!(payload instanceof Auction)) return;
                Auction updated = (Auction) payload;
                ;
                if (updated == null) return;
                Platform.runLater(
                    () -> {
                      for (Auction a : observableList) {
                        if (a.getAuctionId() == updated.getAuctionId()) {
                          a.setStatus(updated.getStatus());
                          break;
                        }
                      }
                    });
              } catch (Exception e) {
                log.error("Không thể tải lại danh sách sản phẩm: {}", e.getMessage(), e);
              }
            } else {
              toast().error(rp.getMessage());
            }
          });
    }

    // ── Tải toàn bộ danh sách ──
    if (rp.getCommand().getClass() == GetAllAuctionsCommand.class) {
      if (rp.isSuccess()) {
        ArrayList<Auction> auctionList = (ArrayList<Auction>) rp.getPayLoad();
        ClientModel.getInstance().isAllAuctionsLoaded = true;
        Platform.runLater(
            () -> {
              observableList.setAll(auctionList);
              itemTable.setItems(observableList);
              updateCountBadge(); // cập nhật badge sau khi tải xong
            });
      } else {
        Platform.runLater(
            () -> {
              toast().error(rp.getMessage());
              if (itemCountBadge != null) itemCountBadge.setText("0 sản phẩm");
            });
      }
      NetworkManager.getInstance().unregister(GetAllAuctionsCommand.class, this);
    }
  }

  private ToastNotifier toast() {
    return ToastNotifier.of(notificationToastController);
  }
}
