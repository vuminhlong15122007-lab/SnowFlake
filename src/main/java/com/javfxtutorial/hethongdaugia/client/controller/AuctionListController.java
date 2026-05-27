package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.Util.AuctionModificationManager;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

public class AuctionListController implements ResponseListener {
    private static final Logger log = LoggerFactory.getLogger(AuctionListController.class);

    // Nhóm trạng thái "đã kết thúc" gộp chung vào 1 nút
    private static final Set<AuctionStatus> ENDED_GROUP = EnumSet.of(
            AuctionStatus.CLOSED,
            AuctionStatus.CANCELLED,
            AuctionStatus.CANCELLED_BY_ADMIN,
            AuctionStatus.PAID
    );

    @FXML private ListView<Auction> featuredProductList;
    @FXML private TextField searchField;
    @FXML private Label sectionTitle;
    @FXML private Button btnAll, btnUpcoming, btnRunning, btnEnded;
    @FXML private ComboBox<String> categoryFilter;

    private ObservableList<Auction> observable;
    private FilteredList<Auction> filterData;
    // null = Tất cả (NOT_START, RUNNING, ENDED_SENTINEL (dùng riêng))
    private AuctionStatus currentStatus = null;
    private boolean filterEndedGroup = false;
    private ScheduledExecutorService statusRefreshScheduler;


    @FXML
    public void initialize() throws ConnectionFailedException {
        NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
        NetworkManager.getInstance().register(AddAuctionCommand.class, this);
        observable = ClientModel.getInstance().getAllAuctions();

        VBox.setVgrow(featuredProductList, Priority.ALWAYS);
        featuredProductList.setMaxWidth(Double.MAX_VALUE);
        featuredProductList.setCellFactory(_ -> new ProductCell());

        filterData = new FilteredList<>(observable, _ -> true);
        featuredProductList.setItems(filterData);

        // Tìm kiếm theo tên
        searchField.textProperty().addListener((_, _, _) -> applyFilters());

        // ComboBox lọc loại sản phẩm
        categoryFilter
                .getItems()
                .addAll("All Products Type", "Art", "Vehicle", "Electronics", "Orther");
        categoryFilter.setValue("All Products Type");
        categoryFilter.valueProperty().addListener((_, _, _) -> applyFilters());
        currentStatus = null;
        applyFilters();

        // Lọc theo trạng thái
        btnAll.setOnAction(_ -> {
            currentStatus = null;
            filterEndedGroup = false;
            sectionTitle.setText("📋  TẤT CẢ PHIÊN ĐẤU GIÁ");
            setActiveButton(btnAll);
            applyFilters();
        });
        btnUpcoming.setOnAction(_ -> {
            currentStatus = AuctionStatus.NOT_START;
            filterEndedGroup = false;
            sectionTitle.setText("📋  PHIÊN SẮP DIỄN RA");
            setActiveButton(btnUpcoming);
            applyFilters();
        });
        btnRunning.setOnAction(_ -> {
            currentStatus = AuctionStatus.RUNNING;
            filterEndedGroup = false;
            sectionTitle.setText("📋  PHIÊN ĐANG DIỄN RA");
            setActiveButton(btnRunning);
            applyFilters();
        });
        // Nút "Kết thúc" gom tất cả: CLOSED, CANCELLED, CANCELLED_BY_ADMIN, DELETED_BY_ADMIN, PAID
        btnEnded.setOnAction(_ -> {
            currentStatus = null;
            filterEndedGroup = true;
            sectionTitle.setText("📋  PHIÊN ĐÃ KẾT THÚC");
            setActiveButton(btnEnded);
            applyFilters();
        });

        setActiveButton(btnAll);
        if (!AuctionModificationManager.getInstance().isAllAuctionsLoaded) {
            loadData();
        }
        // Cứ 30 giây refresh lại status từ server 1 lần
        statusRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auction-status-refresh");
            t.setDaemon(true);
            return t;
        });
        statusRefreshScheduler.scheduleAtFixedRate(() -> {
            try {
                NetworkManager.getInstance().sendRequest(new GetAllAuctionsCommand(), this);
            } catch (Exception e) {
                log.warn("Auto-refresh thất bại: {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // Áp dụng tất cả bộ lọc
    private void applyFilters() {
        filterData.setPredicate(auction -> {
            if (auction == null || auction.getItem() == null)
                return false;
            if (auction.getStatus().equals(AuctionStatus.CANCELLED_BY_ADMIN) || auction.getStatus().equals(AuctionStatus.CANCELLED) || auction.getStatus().equals(AuctionStatus.PAID)) return false;

            // 1. Tìm kiếm theo tên
            String search = searchField.getText();
            if (search != null && !search.isBlank()) {
                String name = auction.getItem().getName();
                if (name == null || !name.toLowerCase().contains(search.toLowerCase())) {
                    return false;
                }
            }

            // 2. Lọc theo trạng thái
            /** LOGIC LỌC
             filterEndedGroup = true   →  bấm nút "Kết thúc"
             currentStatus = null      →  bấm nút "Tất cả"
             currentStatus = NOT_START →  bấm nút "Sắp diễn ra"
             currentStatus = RUNNING   →  bấm nút "Đang diễn ra"
             **/


            if (filterEndedGroup) {
                if (!ENDED_GROUP.contains(auction.getStatus()))
                    return false;
            } else if (currentStatus != null) {
                if (auction.getStatus() != currentStatus)  // chỉ giữ lại nhưng cái có Status trungf vs nút mk bấm ko=> bỏ
                    return false;  // nếu status cuat auction khác nút => bỏ
            }


            // 3. Lọc theo loại sản phẩm
            String selectedCategory = categoryFilter.getValue();
            if (selectedCategory != null && !selectedCategory.equals("All Products Type")) {
                return selectedCategory.equals(getCategoryName(auction));
            }

            return true;
        });
    }

    // Lấy tên loại từ enum Category của Item
    private String getCategoryName(Auction auction) {
        if (auction.getItem() == null || auction.getItem().getCategory() == null) return "Orther";
        return switch (auction.getItem().getCategory()) {
            case ART -> "Art";
            case VEHICLE -> "Vehicle";
            case ELECTRONICS -> "Electronics";
            default -> "Orther";
        };
    }

    private void setActiveButton(Button active) {
        Button[] buttons = {btnAll, btnUpcoming, btnRunning, btnEnded};
        for (Button b : buttons) {
            if (b == null) continue;
            b.setStyle("");
            b.getStyleClass().removeAll("sf-filter-button", "sf-filter-button-active");
            b.getStyleClass().add(b == active ? "sf-filter-button-active" : "sf-filter-button");
        }
    }

    public void loadData() throws ConnectionFailedException {
        Command cmd = new GetAllAuctionsCommand();
        new Thread(() -> {
            try {
                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.sendRequest(cmd, this);
            } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(() ->
                        showAlert("Lỗi", "Không thể gửi yêu cầu", "Loading.gif"));
            } catch (Exception e) {
                log.error("Lỗi load data: {}", e.getMessage(), e);
                Platform.runLater(() ->
                        showAlert("Lỗi", "Tải dữ liệu thất bại", "Loading.gif"));
            }
        }).start();
    }

    public void logOut1(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    public void manageProducts(ActionEvent event) {
        changeScene(
                event, "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductManagement.fxml");
    }

    @FXML
    public void btnHome(ActionEvent event) {
        try {
            changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
        } catch (Exception e) {
            log.error("Không thể chuyển về màn hình chính: {}", e.getMessage(), e);
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
                AuctionModificationManager.getInstance().isAllAuctionsLoaded = true;
            });
        }
        if (rp.getCommand().getClass() == UpdateAuctionStatusCommand.class) {
            Object payload = rp.getPayLoad();
            if (!(payload instanceof Auction)) return;
            Auction updated = (Auction) payload;;
            if (updated == null) return;
            Platform.runLater(() -> {
                for (Auction a : observable) {
                    if (a.getAuctionId() == updated.getAuctionId()) {
                        a.setStatus(updated.getStatus());
                        break;
                    }
                }
            });
        }
    }
}