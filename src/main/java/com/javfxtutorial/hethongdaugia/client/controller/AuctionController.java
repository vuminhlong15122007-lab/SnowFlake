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
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class AuctionController implements ResponseListener {
    @FXML private ListView<Auction> featuredProductList;
    @FXML private TextField searchField;
    @FXML private Label sectionTitle;
    @FXML private Button btnAll, btnUpcoming, btnRunning, btnEnded;
    @FXML private Button btnHome, btnmanageProducts, btnprofile, btnLiveAuction, logOut1;
    @FXML private ComboBox<String> categoryFilter;

    private final ObservableList<Auction> observable = FXCollections.observableArrayList();
    private FilteredList<Auction> filterData;
    private AuctionStatus currentStatus = null;   // null = Tất cả

    @FXML
    public void initialize() {
        VBox.setVgrow(featuredProductList, Priority.ALWAYS);
        featuredProductList.setMaxWidth(Double.MAX_VALUE);
        featuredProductList.setCellFactory(lv -> new ProductCell());
        // Tạo FilteredList
        filterData = new FilteredList<>(observable, auction -> true);
        featuredProductList.setItems(filterData);
        // ========== THÊM: Tìm kiếm ==========
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // ========== THÊM: ComboBox loại sản phẩm ==========
        categoryFilter.getItems().addAll("Tất cả loại", "ART", "VEHICLE", "ELECTRONICS", "Khác");
        categoryFilter.setValue("Tất cả loại");
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // ========== THÊM: Nút lọc trạng thái ==========
        btnAll.setOnAction(e -> {
            currentStatus = null;
            updateSectionTitle("TẤT CẢ PHIÊN ĐẤU GIÁ");
            setActiveButton(btnAll);
            applyFilters();
        });
        btnUpcoming.setOnAction(e -> {
            currentStatus = AuctionStatus.NOT_START;
            updateSectionTitle("PHIÊN SẮP DIỄN RA");
            setActiveButton(btnUpcoming);
            applyFilters();
        });
        btnRunning.setOnAction(e -> {
            currentStatus = AuctionStatus.RUNNING;
            updateSectionTitle("PHIÊN ĐANG DIỄN RA");
            setActiveButton(btnRunning);
            applyFilters();
        });
        btnEnded.setOnAction(e -> {
            currentStatus = AuctionStatus.CLOSED;
            updateSectionTitle("PHIÊN ĐÃ KẾT THÚC");
            setActiveButton(btnEnded);
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

            // 3. Lọc theo loại sản phẩm
            String selectedCategory = categoryFilter.getValue();
            if (selectedCategory != null && !selectedCategory.equals("Tất cả loại")) {
                String auctionCategory = getCategoryName(auction);
                if (!selectedCategory.equals(auctionCategory)) {
                    return false;
                }
            }

            return true;
        });
    }

    // ========== THÊM: Lấy tên loại từ class của Item ==========
    private String getCategoryName(Auction auction) {
        String className = auction.getItem().getClass().getSimpleName();
        switch (className) {
            case "ART": return "ART";
            case "VEHICLE": return "VEHICLE";
            case "ELECTRONICS": return "ELECTRONICS";
            default: return "Khác";
        }
    }

    // ========== THÊM: Cập nhật tiêu đề ==========
    private void updateSectionTitle(String title) {
        if (sectionTitle != null) {
            sectionTitle.setText("📋  " + title);
        }
    }

    // ========== THÊM: Đổi màu nút đang active ==========
    private void setActiveButton(Button active) {
        Button[] buttons = {btnAll, btnUpcoming, btnRunning, btnEnded};
        String activeStyle = "-fx-background-color: linear-gradient(to right, #56ccf2, #2f80ed); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 15;";
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 15; -fx-border-color: #dcdde1; -fx-border-radius: 8;";
        for (Button b : buttons) {
            b.setStyle(b == active ? activeStyle : inactiveStyle);
        }
    }








    public void loadData() {
        Command cmd = new GetAllAuctionsCommand();
        ServerConnection connection = NetworkManager.getConnection();
        new Thread(() -> {
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.register(GetAllAuctionsCommand.class, this);
            connection.sendCommand(cmd);

        }).start();
    }

    public void logOut1(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
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
                if (rp == null) {
                    showAlert("Loi tai du lieu", "Server khong tra ve du lieu phien dau gia.", "Loading.gif");
                    return;
                }

                if (!rp.isSuccess()) {
                    showAlert("Loi tai du lieu", rp.getMessage(), "Loading.gif");
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
