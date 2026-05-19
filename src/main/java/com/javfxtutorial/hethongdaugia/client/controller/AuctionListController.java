package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class AuctionListController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(AuctionListController.class);

  @FXML private ListView<Auction> featuredProductList;
  @FXML private TextField searchField;
  @FXML private Label sectionTitle;
  @FXML private Button btnAll, btnUpcoming, btnRunning, btnEnded;
  @FXML private ComboBox<String> categoryFilter;

  // Lấy observable list từ ClientModel để dùng chung toàn app (Doc 1)
  private ObservableList<Auction> observable;
  private FilteredList<Auction> filterData;
  private AuctionStatus currentStatus = null; // null = Tất cả

  // Tránh gọi server nhiều lần khi navigate qua lại (Doc 1)
  private static boolean isLoaded = false;

  @FXML
  public void initialize() throws ConnectionFailedException {
    observable = ClientModel.getInstance().getAllAuctions();

    VBox.setVgrow(featuredProductList, Priority.ALWAYS);
    featuredProductList.setMaxWidth(Double.MAX_VALUE);
    featuredProductList.setCellFactory(_ -> new ProductCell());

    filterData = new FilteredList<>(observable, _ -> true);
    featuredProductList.setItems(filterData);

    // Tìm kiếm theo tên
    searchField.textProperty().addListener((_, _, _) -> applyFilters());

    // ComboBox lọc loại sản phẩm
    categoryFilter.getItems().addAll("All Products Type", "Art", "Vehicle", "Electronics", "Orther");
    categoryFilter.setValue("All Products Type");
    categoryFilter.valueProperty().addListener((_, _, _) -> applyFilters());

    // Lọc theo trạng thái
    btnAll.setOnAction(_ -> {
      currentStatus = null;
      sectionTitle.setText("📋  TẤT CẢ PHIÊN ĐẤU GIÁ");
      setActiveButton(btnAll);
      applyFilters();
    });
    btnUpcoming.setOnAction(_ -> {
      currentStatus = AuctionStatus.NOT_START;
      sectionTitle.setText("📋  PHIÊN SẮP DIỄN RA");
      setActiveButton(btnUpcoming);
      applyFilters();
    });
    btnRunning.setOnAction(_ -> {
      currentStatus = AuctionStatus.RUNNING;
      sectionTitle.setText("📋  PHIÊN ĐANG DIỄN RA");
      setActiveButton(btnRunning);
      applyFilters();
    });
    btnEnded.setOnAction(_ -> {
      currentStatus = AuctionStatus.CLOSED;
      sectionTitle.setText("📋  PHIÊN ĐÃ KẾT THÚC");
      setActiveButton(btnEnded);
      applyFilters();
    });

    setActiveButton(btnAll);

    if (!isLoaded) {
      loadData();
      isLoaded = true;
    }
  }

  // Áp dụng tất cả bộ lọc
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

      // 3. Lọc theo loại sản phẩm
      // Các else-if sau if đầu không bao giờ chạy được (logic bug ở cả 2 doc),
      // đã sửa lại thành một điều kiện duy nhất cho gọn và đúng
      String selectedCategory = categoryFilter.getValue();
      if (selectedCategory != null && !selectedCategory.equals("All Products Type")) {
        return selectedCategory.equals(getCategoryName(auction));
      }

      return true;
    });
  }

  // Lấy tên loại từ enum Category của Item
  private String getCategoryName(Auction auction) {
    if (auction.getItem() == null || auction.getItem().getCategory() == null)
      return "Orther";
    return switch (auction.getItem().getCategory()) {
      case ART -> "Art";
      case VEHICLE -> "Vehicle";
      case ELECTRONICS -> "Electronics";
      default -> "Orther";
    };
  }

  // Đổi class CSS cho nút đang active — dùng cách sạch của Doc 2,
  // thêm reset setStyle("") của Doc 1 để tránh inline style đè lên CSS class
  private void setActiveButton(Button active) {
    Button[] buttons = {btnAll, btnUpcoming, btnRunning, btnEnded};
    for (Button b : buttons) {
      if (b == null) continue;
      b.setStyle("");
      b.getStyleClass().removeAll("sf-filter-button", "sf-filter-button-active");
      b.getStyleClass().add(b == active ? "sf-filter-button-active" : "sf-filter-button");
    }
  }

  // Gửi request lấy danh sách đấu giá — dùng networkManager.sendRequest() của Doc 1
  public void loadData() throws ConnectionFailedException {
    Command cmd = new GetAllAuctionsCommand();
    new Thread(() -> {
      try {
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.register(GetAllAuctionsCommand.class, this);
        networkManager.sendRequest(cmd, this);
      } catch (SendFailedException e) {
        log.error("Lỗi gửi: {}", e.getMessage());
        Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu", "Loading.gif"));
      } catch (Exception e) {
        log.error("Lỗi load data: {}", e.getMessage(), e);
        Platform.runLater(() -> showAlert("Lỗi", "Tải dữ liệu thất bại", "Loading.gif"));
      }
    }).start();
  }

  public void logOut1(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
  }

  public void manageProducts(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductManagement.fxml");
  }

  @FXML
  public void btnHome(ActionEvent event) {
    try {
      changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void goToProfile(ActionEvent event) {
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/UserInformation.fxml");
  }

  @Override
  public void onResponse(Response rp) {
    if (rp.getCommand().getClass() == GetAllAuctionsCommand.class) {
      Platform.runLater(() -> {
        if (!rp.isSuccess()) {
          showAlert("Lỗi tải dữ liệu", rp.getMessage(), "Loading.gif");
          return;
        }
        ArrayList<Auction> auctions = (ArrayList<Auction>) rp.getPayLoad();
        observable.setAll(auctions);
      });
      NetworkManager networkManager = NetworkManager.getInstance();
      networkManager.unregister(GetAllAuctionsCommand.class, this);
    }
  }
}