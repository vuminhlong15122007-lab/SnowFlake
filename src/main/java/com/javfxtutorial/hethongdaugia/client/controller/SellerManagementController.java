package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.bus.InvalidInputException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.*;
import com.javfxtutorial.hethongdaugia.common.model.domain.*;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.common.model.factory.*;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SellerManagementController implements ResponseListener {
    private static final Logger log = LoggerFactory.getLogger(SellerManagementController.class);
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField tfstepPrice;
    @FXML private DatePicker startDatePicker;
    @FXML private Spinner startHourSpinner;
    @FXML private Spinner startMinuteSpinner;
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner endHourSpinner;
    @FXML private Spinner endMinuteSpinner;
    @FXML private ImageView Image;
    @FXML private ListView<Auction> productList;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private VBox artFields;
    @FXML private TextField artTitleField, artistField, yearCreatedField;
    @FXML private VBox vehicleFields;
    @FXML private TextField licensePlateField, vehicleYearField, brandVehicleField, colorField;
    @FXML private VBox electronicsFields;
    @FXML private TextField brandElecField, modelField;
    @FXML private VBox cancelledInfoBox;
    @FXML private VBox cancelledUserCard;
    @FXML private Label cancelledIcon;
    @FXML private Label cancelledTitle;
    @FXML private Label cancelledSubtitle;
    @FXML private Label cancelledCardTitle;
    @FXML private Label cancelledUserName;
    @FXML private Label cancelledUserSdt;
    @FXML private Label cancelledUserEmail;
    @FXML private Button saveButton;
    @FXML private StackPane badgePane;
    @FXML private Label badgeLabel;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private VBox winnerInfoBox;
    @FXML private VBox detailFormBox;
    @FXML private Label winnerProductName;
    @FXML private Label winnerFinalPrice;
    @FXML private Label winnerName;
    @FXML private Label winnerBidPrice;
    @FXML private Label winnerEmail;
    @FXML private Label winnerSdt;

    private ObservableList<Auction> observable;
    private FilteredList<Auction> filteredList;

    private ObservableList<SellerNotification> notifications;
    private Popup notificationPopup;
    private NotifiCationPopupController popupController;

    private Auction selectedAuction;
    private boolean isLoaded = false;
    private Timeline autoRefreshTimeline;

    private String image =
            "/9j/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEI=";


    public void goMenu(ActionEvent event) {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
    }


    @FXML
    public void initialize() {
        // Ẩn tất cả panel phụ khi khởi động
        if (winnerInfoBox != null)    { winnerInfoBox.setVisible(false);    winnerInfoBox.setManaged(false); }
        if (cancelledInfoBox != null) { cancelledInfoBox.setVisible(false); cancelledInfoBox.setManaged(false); }

        observable = ClientModel.getInstance().getMyAuctions();

        initSpinners();
        initFilterComboBox();
        initCategoryComboBox();

        filteredList = new FilteredList<>(observable, a -> true);
        productList.setItems(filteredList);
        productList.setCellFactory((ListView<Auction> lv) -> new ProductCell2());
        productList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> onAuctionSelected(newVal));

        notifications = ClientModel.getInstance().getSellerNotifications();
        buildNotificationPopup();

        if (!isLoaded) {
            loadMyProducts();
            loadNotificationsFromServer();
            isLoaded = true;
        }
        updateBadge();

        NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
        NetworkManager.getInstance().register(DeleteAuctionCommand.class, this);
        notifications.addListener(
                (ListChangeListener<SellerNotification>) change -> Platform.runLater(this::updateBadge));

        startAutoRefresh();
    }

    private void initSpinners() {
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        startHourSpinner.setEditable(true);
        startMinuteSpinner.setEditable(true);
        endHourSpinner.setEditable(true);
        endMinuteSpinner.setEditable(true);
    }

    private void initCategoryComboBox() {
        categoryComboBox.getItems().addAll("ART", "VEHICLE", "ELECTRONICS", "OTHER");
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showCategoryFields(newVal);
        });
    }


    public void showCategoryFields(String category) {
        hideVbox(artFields); hideVbox(vehicleFields); hideVbox(electronicsFields);
        switch (category) {
            case "ART"         -> showVbox(artFields);
            case "VEHICLE"     -> showVbox(vehicleFields);
            case "ELECTRONICS" -> showVbox(electronicsFields);
        }
    }

    public void hideVbox(VBox vbox) { vbox.setManaged(false); vbox.setVisible(false); }
    public void showVbox(VBox vbox) { vbox.setVisible(true);  vbox.setManaged(true); }



    private void initFilterComboBox() {
        if (filterComboBox == null) return;
        filterComboBox.getItems().addAll(
                "Tất cả", "Chưa bắt đầu", "Đang diễn ra",
                "Chờ thanh toán", "Thành toán thành công",
                "Sản phẩm đã hủy", "Không ai đấu giá");
        filterComboBox.setValue("Tất cả");
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            if (ClientModel.getInstance().getCurrentUser() != null) {
                loadMyProducts();
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }


    /** Ẩn tất cả panel chính, chỉ hiện detailFormBox. */
    private void showDetailForm() {
        if (winnerInfoBox    != null) { winnerInfoBox.setVisible(false);    winnerInfoBox.setManaged(false); }
        if (cancelledInfoBox != null) { cancelledInfoBox.setVisible(false); cancelledInfoBox.setManaged(false); }
        if (detailFormBox    != null) { detailFormBox.setVisible(true);     detailFormBox.setManaged(true); }
    }

    /** Ẩn tất cả panel chính, chỉ hiện winnerInfoBox. */
    private void showWinnerPanel() {
        if (detailFormBox    != null) { detailFormBox.setVisible(false);    detailFormBox.setManaged(false); }
        if (cancelledInfoBox != null) { cancelledInfoBox.setVisible(false); cancelledInfoBox.setManaged(false); }
        if (winnerInfoBox    != null) { winnerInfoBox.setVisible(true);     winnerInfoBox.setManaged(true); }
    }

    /** Ẩn tất cả panel chính, chỉ hiện cancelledInfoBox. */
    private void showCancelledPanel(String icon, String title, String subtitle, boolean showUserCard, String cardColor, String cardTitle, Auction auction) {
        if (winnerInfoBox != null) { winnerInfoBox.setVisible(false); winnerInfoBox.setManaged(false); }
        if (detailFormBox != null) { detailFormBox.setVisible(false); detailFormBox.setManaged(false); }

        cancelledIcon.setText(icon);
        cancelledTitle.setText(title);
        cancelledSubtitle.setText(subtitle);

        if (showUserCard && auction != null) {
            cancelledUserCard.setStyle(String.format("-fx-background-color: linear-gradient(to right, %s-soft, -sf-surface);" +
                            "-fx-background-radius: 14; -fx-border-color: %s;" + "-fx-border-radius: 14; -fx-border-width: 1.5; -fx-padding: 16;", cardColor, cardColor));
            cancelledCardTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 0 0 4 0;" + "-fx-text-fill: " + cardColor + ";");

            cancelledCardTitle.setText(cardTitle);
            cancelledUserName.setText(nullOrEmpty(auction.getWinnerName())  ? "Null"        : auction.getWinnerName());
            cancelledUserSdt.setText(nullOrEmpty(auction.getWinnerSdt())    ? "Null"  : auction.getWinnerSdt());
            cancelledUserEmail.setText(nullOrEmpty(auction.getWinnerEmail()) ? "Null" : auction.getWinnerEmail());
            cancelledUserCard.setVisible(true);
            cancelledUserCard.setManaged(true);
        } else {
            cancelledUserCard.setVisible(false);
            cancelledUserCard.setManaged(false);
        }

        cancelledInfoBox.setVisible(true);
        cancelledInfoBox.setManaged(true);
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isBlank();
    }


    /**
     * Luồng xử lý khi chọn phiên:
     *  CANCELLED (có winner)    → cancelledInfoBox với thông tin người vi phạm
     *  CANCELLED_BY_ADMIN       → cancelledInfoBox không có user card
     *  CLOSED có winner         → winnerInfoBox (chờ thanh toán)
     *  CLOSED không winner      → cancelledInfoBox "không ai tham gia"
     *  PAID                     → winnerInfoBox (đã thanh toán)
     *  NOT_START                → detailFormBox (cho phép sửa)
     *  RUNNING                  → detailFormBox (chỉ xem, không sửa)
     */
    private void onAuctionSelected(Auction auction) {
        if (auction == null) return;
        switch (auction.getStatus()) {
            case CANCELLED          -> handleCancelledAuction(auction);
            case CANCELLED_BY_ADMIN -> handleCancelledByAdminAuction(auction);
            case CLOSED             -> handleClosedAuction(auction);
            case PAID               -> showWinnerInfo(auction);
            case RUNNING            -> handleReadOnlyAuction(auction);
            default                 -> handleEditableAuction(auction);
        }
    }

    private void handleCancelledAuction(Auction auction) {
        selectedAuction = auction;
        boolean hasWinner = !nullOrEmpty(auction.getWinnerName());
        if (hasWinner) {
            showCancelledPanel(
                    "🚫", "Phiên bị hủy do không thanh toán",
                    "Người thắng đã không hoàn tất thanh toán",
                    true, "-sf-danger", "👤  THÔNG TIN NGƯỜI VI PHẠM", auction);
        } else {
            showCancelledPanel(
                    "🚫", "Phiên bị hủy",
                    "Phiên đấu giá đã bị hủy do không có ai tham gia",
                    false, null, null, null);
        }
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        maybeAddNotification(auction);
    }

    private void handleCancelledByAdminAuction(Auction auction) {
        selectedAuction = auction;
        showCancelledPanel(
                "⛔", "Phiên bị hủy bởi Admin",
                "Phiên đấu giá vi phạm tiêu chuẩn cộng đồng",
                false, null, null, null);
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        maybeAddNotification(auction);
    }

    private void handleClosedAuction(Auction auction) {
        selectedAuction = auction;
        boolean hasWinner = !nullOrEmpty(auction.getWinnerName());
        if (hasWinner) {
            showWinnerInfo(auction);
        } else {
            showCancelledPanel(
                    "😞", "Không có ai tham gia đấu giá",
                    "Phiên đã kết thúc nhưng không có lượt đặt giá nào",
                    false, null, null, null);
            saveButton.setVisible(false);
            saveButton.setManaged(false);
        }
        maybeAddNotification(auction);
    }

    private void handleEditableAuction(Auction auction) {
        selectedAuction = auction;
        hienThiChiTietSanPham(auction);
        showDetailForm();
        detailFormBox.setDisable(false);
        setupEditButton();
        maybeAddNotification(auction);
    }

    /** RUNNING → hiển thị form nhưng không cho sửa */
    private void handleReadOnlyAuction(Auction auction) {
        selectedAuction = auction;
        hienThiChiTietSanPham(auction);
        showDetailForm();
        detailFormBox.setDisable(true);
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        maybeAddNotification(auction);
    }

    /** Thiết lập nút Save sang chế độ "Sửa". */
    private void setupEditButton() {
        saveButton.setVisible(true);
        saveButton.setManaged(true);
        saveButton.setStyle(
                "-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold;"
                        + " -fx-background-radius: 5; -fx-padding: 5 0; -fx-font-size: 12px;");
        saveButton.setText("Sửa");
        saveButton.setOnAction(event -> {
            try { suaSp(event); } catch (Exception e) { log.error("Lỗi sửa sp: {}", e.getMessage(), e); }
        });
    }

    /** Thêm notification nếu phiên đã ở trạng thái cần thông báo. */
    private void maybeAddNotification(Auction auction) {
        AuctionStatus st = auction.getStatus();
        if (st != AuctionStatus.CLOSED && st != AuctionStatus.PAID
                && st != AuctionStatus.CANCELLED && st != AuctionStatus.CANCELLED_BY_ADMIN)
            return;

        SellerNotification.Type type = switch (st) {
            case CLOSED          -> SellerNotification.Type.CLOSED;
            case PAID            -> SellerNotification.Type.PAID;
            case CANCELLED_BY_ADMIN -> SellerNotification.Type.CANCELLED_BY_ADMIN;
            default              -> SellerNotification.Type.CANCELLED;
        };
        String pName = auction.getItem() != null ? auction.getItem().getName() : String.valueOf(auction.getAuctionId());
        String wName = !nullOrEmpty(auction.getWinnerName()) ? auction.getWinnerName() : "N/A";
        addOrReplaceNotification(new SellerNotification(auction.getAuctionId(), type, pName, wName, auction.getWinningPrice()));
    }

    private void addOrReplaceNotification(SellerNotification notif) {
        SellerNotification existing = notifications.stream().filter(n -> n.getAuctionId() == notif.getAuctionId()).findFirst().orElse(null);

        if (existing == null) {
            if (ClientModel.getInstance().isNotificationRead(notif.getNotificationId())) notif.setRead(true);
            notifications.add(0, notif);
        } else if (priority(notif.getType()) > priority(existing.getType())) {
            notif.setRead(false);
            ClientModel.getInstance().markNotificationUnread(notif.getAuctionId());
            notifications.remove(existing);
            notifications.add(0, notif);
        }
    }



    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        tfstepPrice.clear();
        Platform.runLater(() -> { startDatePicker.setValue(null); endDatePicker.setValue(null); });
        startHourSpinner.getValueFactory().setValue(0);
        startMinuteSpinner.getValueFactory().setValue(0);
        endHourSpinner.getValueFactory().setValue(0);
        endMinuteSpinner.getValueFactory().setValue(0);
        categoryComboBox.setValue(null);
        Image.setImage(null);
        productList.getSelectionModel().clearSelection();
        image = "/9j/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEI=";
        hideVbox(artFields); hideVbox(vehicleFields); hideVbox(electronicsFields);
        artTitleField.clear(); artistField.clear(); yearCreatedField.clear();
        licensePlateField.clear(); vehicleYearField.clear(); brandVehicleField.clear(); colorField.clear();
        brandElecField.clear(); modelField.clear();
        selectedAuction = null;
    }

    @FXML
    public void onAddButton(ActionEvent event) {
        showDetailForm();
        saveButton.setVisible(true);
        saveButton.setManaged(true);
        saveButton.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;"
                        + " -fx-background-radius: 5; -fx-padding: 5 0; -fx-font-size: 12px;");
        saveButton.setText("Lưu");
        saveButton.setOnAction(_ -> {
            try { onSaveButton(event); } catch (IOException e) { throw new RuntimeException(e); }
        });
        clearForm();
    }

    public Auction getInfo() throws Exception {
        //Các thông tin cơ bản của phiên đấu giá
        String rawPrice2 = priceField.getText().replaceAll("[^0-9.]", "");
        if (rawPrice2.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập giá khởi điểm."); return null; }
        BigDecimal initPrice = new BigDecimal(rawPrice2);

        String rawStep = tfstepPrice.getText().replaceAll("[^0-9.]", "");
        if (rawStep.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập bước giá."); return null; }
        BigDecimal stepPrice = new BigDecimal(rawStep);

        LocalDate ngayBD = startDatePicker.getValue();
        LocalDate ngayKT = endDatePicker.getValue();
        int startHour = (int) startHourSpinner.getValue();
        int startMinu = (int) startMinuteSpinner.getValue();
        int endHour   = (int) endHourSpinner.getValue();
        int endMinu   = (int) endMinuteSpinner.getValue();

        if (ngayBD == null) { showAlert("Lỗi", "Vui lòng chọn ngày bắt đầu."); return null; }
        if (ngayKT == null) { showAlert("Lỗi", "Vui lòng chọn ngày kết thúc."); return null; }

        LocalDateTime tGianBD = LocalDateTime.of(ngayBD, LocalTime.of(startHour, startMinu));
        LocalDateTime tGianKT = LocalDateTime.of(ngayKT, LocalTime.of(endHour, endMinu));

        if (tGianBD.isAfter(tGianKT)) {
            showAlert("Lỗi", "Thời gian bắt đầu không được sau thời gian kết thúc");
            throw new InvalidInputException("startTime", String.valueOf(tGianBD), "Thời gian bắt đầu không được sau thời gian kết thúc");
        }
        if (tGianBD.isBefore(LocalDateTime.now())) {
            showAlert("Lỗi", "Thời gian bắt đầu phải sau thời gian bây giờ");
            throw new InvalidInputException("startTime", String.valueOf(tGianBD), "Thời gian bắt đầu phải sau thời gian hiện tại");
        }

        //Lấy thông tin Item cơ bản
        int sellerId      = ClientModel.getInstance().getCurrentUser().getId();
        String sellerName = ClientModel.getInstance().getCurrentUser().getName();
        int itemId   = (selectedAuction == null) ? 0 : selectedAuction.getItem().getItemId();
        String name = nameField.getText();
        String description = descriptionField.getText();
        String category = categoryComboBox.getValue();
        if (category == null) { showAlert("Lỗi", "Vui lòng chọn danh mục sản phẩm!", "Wait.gif"); return null; }


        Item baseItem = new Item( sellerName, sellerId, itemId, name, description, image, ItemCategory.valueOf(category));
        ItemFactory factory = switch (category) {
            case "ART"         -> new ArtFactory( baseItem, artTitleField, artistField, yearCreatedField);
            case "VEHICLE"     -> new VehicleFactory( baseItem, licensePlateField, vehicleYearField, brandVehicleField, colorField);
            case "ELECTRONICS" -> new ElectronicsFactory( baseItem, brandElecField, modelField);
            default            -> new OtherItemFactory(baseItem);
        };
        Item item = factory.createItemFromForm();
        return new Auction(item, sellerId, initPrice, stepPrice, tGianBD, tGianKT, AuctionStatus.NOT_START);
    }



    @FXML
    public void onSaveButton(ActionEvent event) throws IOException {
        try {
            Auction auction = getInfo();
            if (auction == null) return;
            new Thread(() -> {
                try {
                    ServerConnection connection = NetworkManager.getConnection();
                    NetworkManager.getInstance().register(AddAuctionCommand.class, this);
                    AddAuctionCommand cm = new AddAuctionCommand();
                    cm.addData("Auction", auction);
                    connection.sendCommand(cm);
                } catch (ConnectionFailedException e) {
                    log.error("Lỗi kết nối: {}", e.getMessage());
                    Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối server"));
                } catch (SendFailedException e) {
                    log.error("Lỗi gửi: {}", e.getMessage());
                    Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu"));
                } catch (Exception e) {
                    log.error("Lỗi: {}", e.getMessage(), e);
                    Platform.runLater(() -> showAlert("Lỗi", "Thêm sản phẩm thất bại: " + e.getMessage()));
                }
            }).start();
        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Vui lòng nhập số hợp lệ");
        } catch (Exception e) {
            log.error("Lỗi: {}", e.getMessage(), e);
        }
    }

    @FXML
    public void suaSp(ActionEvent event) throws Exception {
        if (selectedAuction == null) {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm cần sửa!", "Wait.gif");
            return;
        }
        if (!(selectedAuction.getStatus() == AuctionStatus.NOT_START)) {
            showAlert("Không thể sửa", "Không thể sửa sản phẩm đang chạy hoặc đã kết thúc", "Wrong.gif");
            return;
        }

        Auction auction = getInfo();
        if (auction == null) return;
        auction.setAuctionId(selectedAuction.getAuctionId());
        auction.getItem().setItemId(selectedAuction.getItem().getItemId());
        new Thread(() -> {
            try {
                ServerConnection connection = NetworkManager.getConnection();
                NetworkManager.getInstance().register(UpdateAuctionCommand.class, this);
                connection.sendCommand(new UpdateAuctionCommand(auction));
            } catch (ConnectionFailedException e) {
                log.error("Lỗi kết nối: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối server"));
            } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu sửa"));
            } catch (Exception e) {
                log.error("Lỗi sửa auction: {}", e.getMessage(), e);
                Platform.runLater(() -> showAlert("Lỗi", "Sửa thất bại: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void deleteAuction() {
        selectedAuction = productList.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm cần xóa!", "Wait.gif");
            return;
        }
        new Thread(() -> {
            try {
                ServerConnection connection = NetworkManager.getConnection();
                NetworkManager.getInstance().register(DeleteAuctionCommand.class, this);
                connection.sendCommand(new DeleteAuctionCommand(selectedAuction));
            } catch (ConnectionFailedException e) {
                log.error("Lỗi kết nối: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối server"));
            } catch (SendFailedException e) {
                log.error("Lỗi gửi: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu xóa"));
            } catch (Exception e) {
                log.error("Lỗi xóa auction: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi", "Không thể xóa"));
            }
        }).start();
    }

    private void loadMyProducts() {
        int sellerId = ClientModel.getInstance().getCurrentUser().getId();
        new Thread(() -> {
            try {
                Command cmd = new GetAuctionsBySellerIdCommand();
                cmd.addData("sellerId", sellerId);
                NetworkManager.getInstance().sendRequest(cmd, this);
            } catch (ConnectionFailedException e) {
                log.error("Lỗi kết nối khi load sản phẩm: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối đến server"));
            } catch (SendFailedException e) {
                log.error("Lỗi gửi command load sản phẩm: {}", e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu tải dữ liệu"));
            } catch (Exception e) {
                log.error("Lỗi không xác định khi load sản phẩm: {}", e.getMessage(), e);
                Platform.runLater(() -> showAlert("Lỗi", "Tải sản phẩm thất bại: " + e.getMessage()));
            }
        }).start();
    }

    // ── Image ─────────────────────────────────────────────────────────────────

    @FXML
    public void clickToChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chon anh dai dien");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) return;
        try {
            byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
            image = ImageHelper.fileToBase64(fileContent);
            if (Image != null) Image.setImage(new Image(new ByteArrayInputStream(fileContent)));
        } catch (Exception e) {
            log.error("Không thể tải ảnh lên: {}", e.getMessage(), e);
            showAlert("Lỗi", "Không thể tải ảnh lên!", "False.gif");
        }
    }



    private void showWinnerInfo(Auction auction) {
        winnerProductName.setText(auction.getItem() != null ? auction.getItem().getName() : "—");

        BigDecimal wPrice = auction.getWinningPrice();
        String priceStr = (wPrice != null)
                ? String.format("%,.0f VNĐ", wPrice)
                : String.format("%,.0f VNĐ", auction.getCurrentPrice());
        winnerFinalPrice.setText(priceStr);
        winnerBidPrice.setText(priceStr);

        winnerName.setText(nullOrEmpty(auction.getWinnerName()) ? "—" : auction.getWinnerName());
        if (winnerEmail != null) winnerEmail.setText(nullOrEmpty(auction.getWinnerEmail()) ? "—" : auction.getWinnerEmail());
        if (winnerSdt   != null) winnerSdt.setText(nullOrEmpty(auction.getWinnerSdt())     ? "—" : auction.getWinnerSdt());

        showWinnerPanel();
    }



    @FXML
    public void onBackToForm() {
        if (winnerInfoBox    != null) { winnerInfoBox.setVisible(false);    winnerInfoBox.setManaged(false); }
        if (cancelledInfoBox != null) { cancelledInfoBox.setVisible(false); cancelledInfoBox.setManaged(false); }
        if (detailFormBox    != null) { detailFormBox.setVisible(true);     detailFormBox.setManaged(true); }

        // Điền lại thông tin sản phẩm đang chọn vào form
        if (selectedAuction != null) {
            hienThiChiTietSanPham(selectedAuction);
            setupEditButton();
        } else {
            productList.getSelectionModel().clearSelection();
        }
    }

    public void hienThiChiTietSanPham(Auction auction) {
        Item item = auction.getItem();
        nameField.setText(item.getName());
        descriptionField.setText(item.getDescription());
        priceField.setText(String.valueOf(auction.getCurrentPrice()));
        tfstepPrice.setText(String.valueOf(auction.getStepPrice()));
        saveButton.setText("Sửa");

        LocalDateTime start = auction.getStartingTime();
        if (start != null) {
            startDatePicker.setValue(start.toLocalDate());
            startHourSpinner.getValueFactory().setValue(start.getHour());
            startMinuteSpinner.getValueFactory().setValue(start.getMinute());
        }
        LocalDateTime end = auction.getEndingTime();
        if (end != null) {
            endDatePicker.setValue(end.toLocalDate());
            endHourSpinner.getValueFactory().setValue(end.getHour());
            endMinuteSpinner.getValueFactory().setValue(end.getMinute());
        }

        String base64 = item.getImage();
        if (base64 != null && !base64.isEmpty()) {
            byte[] imgBytes = Base64.getDecoder().decode(base64);
            Image.setImage(new Image(new ByteArrayInputStream(imgBytes)));
        }
        ItemCategory cate = item.getCategory();
        categoryComboBox.getSelectionModel().select(String.valueOf(cate));
        showCategoryFields(String.valueOf(cate));


        ItemFactory factory = switch (cate) {
            case ItemCategory.ART         -> new ArtFactory( item, artTitleField, artistField, yearCreatedField);
            case ItemCategory.VEHICLE     -> new VehicleFactory( item, licensePlateField, vehicleYearField, brandVehicleField, colorField);
            case ItemCategory.ELECTRONICS -> new ElectronicsFactory( item, brandElecField, modelField);
            default            -> new OtherItemFactory(item);
        };
        factory.showData();
    }



    private void applyFilter(String filter) {
        if (filteredList == null) return;
        filteredList.setPredicate(a -> {
            if (filter == null || filter.equals("Tất cả")) return true;
            AuctionStatus st = a.getStatus();
            boolean hasWinner = !nullOrEmpty(a.getWinnerName());
            return switch (filter) {
                case "Chưa bắt đầu"          -> st == AuctionStatus.NOT_START;
                case "Đang diễn ra"           -> st == AuctionStatus.RUNNING;
                case "Chờ thanh toán"         -> st == AuctionStatus.CLOSED && hasWinner;
                case "Thành toán thành công"  -> st == AuctionStatus.PAID;
                case "Sản phẩm đã hủy"        -> st == AuctionStatus.CANCELLED || st == AuctionStatus.CANCELLED_BY_ADMIN;
                case "Không ai đấu giá"       -> st == AuctionStatus.CLOSED && !hasWinner;
                default -> true;
            };
        });
    }





    private static int priority(SellerNotification.Type type) {
        return switch (type) {
            case PAID            -> 2;
            case CANCELLED       -> 1;
            case CLOSED          -> 0;
            case CANCELLED_BY_ADMIN -> 3;
        };
    }

    private void buildNotificationPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/javfxtutorial/hethongdaugia/view/fxml/NotifiCationPopup.fxml"));
            VBox popupRoot = loader.load();
            popupController = loader.getController();
            popupController.setOnMarkRead(notif -> {
                markNotificationReadOnServer(notif.getAuctionId());
                Platform.runLater(this::updateBadge);
            });
            notificationPopup = new Popup();
            notificationPopup.setAutoHide(true);
            notificationPopup.getContent().add(popupRoot);
        } catch (IOException e) {
            log.error("Không load được NotificationPopup.fxml: {}", e.getMessage(), e);
        }
    }

    private void updateBadge() {
        if (badgeLabel == null) return;
        int unread = (int) notifications.stream().filter(n -> !n.isRead()).count();
        badgeLabel.setText((unread > 99) ? "99+" : String.valueOf(unread));
        badgeLabel.setVisible(unread > 0);
        if (badgePane != null) badgePane.setVisible(unread > 0);
    }

    @FXML
    public void openNotifications(MouseEvent event) {
        if (notificationPopup == null || popupController == null) return;
        popupController.loadNotifications(notifications);
        if (notificationPopup.isShowing()) {
            notificationPopup.hide();
        } else {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Bounds bounds = source.localToScreen(source.getBoundsInLocal());
            notificationPopup.show(stage, bounds.getMaxX() - 400, bounds.getMaxY() + 8);
        }
    }

    // ── onResponse ────────────────────────────────────────────────────────────

    @Override
    public void onResponse(Response rp) {
        Class<?> cmdClass = rp.getCommand().getClass();
        if      (cmdClass == AddAuctionCommand.class)                    handleAddAuctionResponse(rp);
        else if (cmdClass == GetAuctionsBySellerIdCommand.class)         handleLoadAuctionsResponse(rp);
        else if (cmdClass == DeleteAuctionCommand.class)                 handleDeleteAuctionResponse(rp);
        else if (cmdClass == UpdateAuctionCommand.class)                 handleUpdateAuctionResponse(rp);
        else if (cmdClass == UpdateAuctionStatusCommand.class)           handleAuctionStatusPushResponse(rp);
        else if (cmdClass == GetSellerNotificationsCommand.class)        handleGetNotificationsResponse(rp);
        else if (cmdClass == MarkNotificationReadCommand.class)          handleMarkNotificationReadResponse(rp);
    }

    private void handleAddAuctionResponse(Response rp) {
        NetworkManager.getInstance().unregister(AddAuctionCommand.class, this);
        Platform.runLater(() -> {
            if (rp.isSuccess()) {
                observable.add((Auction) rp.getPayLoad());
                showAlert("Thành công", "Thêm sản phẩm thành công!", "Happy.gif");
            } else {
                log.warn("Thêm thất bại: {}", rp.getMessage());
                showAlert("Thất bại", rp.getMessage(), "Wait.gif");
            }
        });
    }

    private void handleLoadAuctionsResponse(Response rp) {
        NetworkManager.getInstance().unregister(GetAuctionsBySellerIdCommand.class, this);
        if (!rp.isSuccess()) return;
        Platform.runLater(() -> {
            ArrayList<Auction> auctions = (ArrayList<Auction>) rp.getPayLoad();
            observable.setAll(auctions);
            syncNotificationsFromAuctions(auctions);
            sortNotifications();
            updateBadge();
        });
    }

    private void syncNotificationsFromAuctions(List<Auction> auctions) {
        for (Auction a : auctions) {
            AuctionStatus st = a.getStatus();
            if (st != AuctionStatus.CLOSED && st != AuctionStatus.PAID
                    && st != AuctionStatus.CANCELLED && st != AuctionStatus.CANCELLED_BY_ADMIN)
                continue;

            SellerNotification.Type type = switch (st) {
                case CLOSED          -> SellerNotification.Type.CLOSED;
                case PAID            -> SellerNotification.Type.PAID;
                case CANCELLED_BY_ADMIN -> SellerNotification.Type.CANCELLED_BY_ADMIN;
                default              -> SellerNotification.Type.CANCELLED;
            };
            String pName = a.getItem() != null ? a.getItem().getName() : String.valueOf(a.getAuctionId());
            String wName = !nullOrEmpty(a.getWinnerName()) ? a.getWinnerName() : "N/A";

            SellerNotification notif = new SellerNotification(a.getAuctionId(), type, pName, wName, a.getWinningPrice());
            if (a.getEndingTime() != null) notif.setCreatedAt(a.getEndingTime());
            if (ClientModel.getInstance().isNotificationReadByAuction(a.getAuctionId())) notif.setRead(true);

            Optional<SellerNotification> existing = notifications.stream()
                    .filter(n -> n.getAuctionId() == a.getAuctionId()).findFirst();

            if (existing.isPresent()) {
                if (priority(notif.getType()) > priority(existing.get().getType())) {
                    notif.setRead(false);
                    ClientModel.getInstance().markNotificationUnread(a.getAuctionId());
                    notifications.remove(existing.get());
                    notifications.add(notif);
                }
            } else {
                notifications.add(notif);
            }
        }
    }

    private void sortNotifications() {
        notifications.sort((n1, n2) -> {
            if (n1.isRead() != n2.isRead()) return n1.isRead() ? 1 : -1;
            if (n1.getCreatedAt() != null && n2.getCreatedAt() != null)
                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
            return 0;
        });
    }

    private void handleDeleteAuctionResponse(Response rp) {
        if ("ADMIN_DELETED_PRODUCT".equals(rp.getMessage()) && rp.getPayLoad() instanceof SellerNotification adminNotif) {
            Platform.runLater(() -> {
                observable.removeIf(a -> a.getAuctionId() == adminNotif.getAuctionId());
                addOrReplaceNotification(adminNotif);
            });
            return;
        }
        NetworkManager.getInstance().unregister(DeleteAuctionCommand.class, this);
        Platform.runLater(() -> {
            if (rp.isSuccess()) {
                Auction deleted = (rp.getPayLoad() instanceof Auction) ? (Auction) rp.getPayLoad() : selectedAuction;
                observable.remove(deleted);
                selectedAuction = null;
                showAlert("Thành công", "Xóa sản phẩm thành công!", "Happy.gif");
            } else {
                showAlert("Thất bại", rp.getMessage(), "Wait.gif");
            }
        });
    }

    private void handleUpdateAuctionResponse(Response rp) {
        NetworkManager.getInstance().unregister(UpdateAuctionCommand.class, this);
        Platform.runLater(() -> {
            if (rp.isSuccess()) {
                Auction updated = (Auction) rp.getPayLoad();
                int index = observable.indexOf(selectedAuction);
                if (index >= 0) observable.set(index, updated);
                selectedAuction = updated;
                productList.refresh();
                showAlert("Thành công", "Sửa sản phẩm thành công", "Happy.gif");
            } else {
                showAlert("Thất bại", rp.getMessage(), "Wait.gif");
            }
        });
    }

    private void handleAuctionStatusPushResponse(Response rp) {
        if ("ADMIN_CANCELLED_AUCTION".equals(rp.getMessage()) && rp.getPayLoad() instanceof SellerNotification adminNotif) {
            Platform.runLater(() -> {
                for (int i = 0; i < observable.size(); i++) {
                    if (observable.get(i).getAuctionId() == adminNotif.getAuctionId()) {
                        observable.get(i).setStatus(AuctionStatus.CANCELLED_BY_ADMIN);
                        observable.set(i, observable.get(i));
                        break;
                    }
                }
                addOrReplaceNotification(adminNotif);
            });
            return;
        }

        if (!(rp.getPayLoad() instanceof Auction auction)) return;
        AuctionStatus status = auction.getStatus();
        if (status != AuctionStatus.CLOSED && status != AuctionStatus.PAID
                && status != AuctionStatus.CANCELLED && status != AuctionStatus.CANCELLED_BY_ADMIN)
            return;

        SellerNotification.Type type = switch (status) {
            case CLOSED          -> SellerNotification.Type.CLOSED;
            case PAID            -> SellerNotification.Type.PAID;
            case CANCELLED_BY_ADMIN -> SellerNotification.Type.CANCELLED_BY_ADMIN;
            default              -> SellerNotification.Type.CANCELLED;
        };
        String productName  = auction.getItem() != null ? auction.getItem().getName() : String.valueOf(auction.getAuctionId());
        String winnerNameStr = !nullOrEmpty(auction.getWinnerName()) ? auction.getWinnerName() : "N/A";
        SellerNotification notif = new SellerNotification(
                auction.getAuctionId(), type, productName, winnerNameStr, auction.getWinningPrice());

        Platform.runLater(() -> {
            addOrReplaceNotification(notif);
            for (int i = 0; i < observable.size(); i++) {
                if (observable.get(i).getAuctionId() == auction.getAuctionId()) {
                    observable.set(i, auction);
                    break;
                }
            }
        });
    }

    // ── Notification từ DB ────────────────────────────────────────────────────

    private void loadNotificationsFromServer() {
        int sellerId = ClientModel.getInstance().getCurrentUser().getId();
        new Thread(() -> {
            try {
                Command cmd = new GetSellerNotificationsCommand();
                cmd.addData("sellerId", sellerId);
                NetworkManager.getInstance().sendRequest(cmd, this);
            } catch (Exception e) {
                log.error("Lỗi load notifications từ server: {}", e.getMessage(), e);
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void handleGetNotificationsResponse(Response rp) {
        NetworkManager.getInstance().unregister(GetSellerNotificationsCommand.class, this);
        if (!rp.isSuccess() || rp.getPayLoad() == null) return;
        Platform.runLater(() -> {
            List<SellerNotification> fromDb = (List<SellerNotification>) rp.getPayLoad();
            for (SellerNotification n : fromDb) {
                // Đồng bộ trạng thái is_read từ DB vào local Preferences
                if (n.isRead()) {
                    ClientModel.getInstance().markNotificationReadByAuction(n.getAuctionId());
                }
                Optional<SellerNotification> existing = notifications.stream()
                        .filter(x -> x.getAuctionId() == n.getAuctionId()).findFirst();
                if (existing.isEmpty()) {
                    notifications.add(n);
                }
            }
            sortNotifications();
            updateBadge();
        });
    }

    private void handleMarkNotificationReadResponse(Response rp) {
        NetworkManager.getInstance().unregister(MarkNotificationReadCommand.class, this);
        if (!rp.isSuccess()) {
            log.warn("markAsRead thất bại: {}", rp.getMessage());
        }
    }

    /** Gửi markAsRead lên server khi seller click vào notification */
    private void markNotificationReadOnServer(int auctionId) {
        new Thread(() -> {
            try {
                Command cmd = new MarkNotificationReadCommand();
                cmd.addData("auctionId", auctionId);
                NetworkManager.getInstance().sendRequest(cmd, this);
            } catch (Exception e) {
                log.error("Lỗi gửi markAsRead: {}", e.getMessage(), e);
            }
        }).start();
    }
}