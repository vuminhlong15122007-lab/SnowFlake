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

    // ── FXML fields ───────────────────────────────────────────────────────────
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
    @FXML private TextArea message;
    @FXML private Button saveButton;
    @FXML private StackPane badgePane;
    @FXML private Label badgeLabel;
    @FXML private ComboBox<String> filterComboBox;

    @FXML private VBox winnerInfoBox;
    @FXML private VBox detailFormBox;
    @FXML private Label winnerProductName;
    @FXML private Label winnerFinalPrice;
    @FXML private Label winnerName;
    @FXML private Label winnerId;
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

    // Base64 ảnh mặc định
    private String image =
        "/9j/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEI=";


    public void goMenu(ActionEvent event) {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
    }

    // ── Initialize ────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        if (winnerInfoBox != null) {
            winnerInfoBox.setVisible(false);
            winnerInfoBox.setManaged(false);
        }
        message.setVisible(false);
        message.setManaged(false);

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

    // ── Selection handler ─────────────────────────────────────────────────────

    /**
     * Xử lý khi người dùng chọn một phiên đấu giá trong danh sách.
     *
     * Luồng xử lý:
     *  - CANCELLED (không có winner) → alert "không ai tham gia", không làm gì thêm
     *  - CANCELLED (có winner)       → alert "người thắng không thanh toán", không làm gì thêm
     *  - CANCELLED_BY_ADMIN          → alert "admin hủy", không làm gì thêm
     *  - CLOSED không có winner      → alert "không ai đặt giá", không làm gì thêm
     *  - CLOSED có winner / PAID     → hiển thị panel thông tin người thắng
     *  - NOT_START / RUNNING         → hiển thị form chi tiết để chỉnh sửa
     */
    private void onAuctionSelected(Auction auction) {
        if (auction == null) return;
        AuctionStatus status = auction.getStatus();

        switch (status) {
            case CANCELLED -> handleGeneralCancelledAuction(auction);
            case CANCELLED_BY_ADMIN -> handleGeneralCancelledAuction(auction);
            case CLOSED -> handleClosedAuction(auction);
            case PAID -> showWinnerInfo(auction); // Đã thanh toán → luôn hiện thông tin người thắng
            default -> handleEditableAuction(auction); // NOT_START, RUNNING → cho phép sửa
        }
    }

    /** CANCELLED:  hủy do không thanh toán. */
    private void setMessageCancelledAuction(Auction auction) {
        message.setText(String.format("Phiên đấu giá bị hủy do người dùng %s không thanh toán \n" +
            "Thông tin chi tiết người dùng: \n" +
            "Tên: %s \n" +
            "Số điện thoại: %s \n" +
            "Email: %s",  auction.getWinnerName(), auction.getWinnerName(), auction.getWinnerSdt(), auction.getWinnerEmail()));
    }
    /** CANCELLED_BY_ADMIN:  hủy do admin xoa. */
    private void setMessageCancelledByAdminAuction(Auction auction) {
        message.setText(String.format("Phiên đấu giá bị hủy do người dùng vi phạm tiêu chuẩn cộng đồng"));
    }

    /**
     * CLOSED: phân biệt có winner hay không.
     *  - Có winner → hiển thị panel thông tin người thắng (chờ thanh toán)
     *  - Không có winner → alert "không ai đặt giá"
     */
    private void handleClosedAuction(Auction auction) {
        boolean hasWinner = auction.getWinnerName() != null && !auction.getWinnerName().isBlank();
        if (hasWinner) {
            showWinnerInfo(auction);
        } else {
            selectedAuction = auction;
            hienThiChiTietSanPham(auction);
            showDetailForm();
            saveButton.setVisible(false);
            message.setText("Phiên đấu giá không có ai tham gia");
            maybeAddNotification(auction);
        }
    }

    /**
     * NOT_START / RUNNING: hiển thị form chi tiết để người dùng có thể sửa.
     * Cũng thiết lập nút Save thành chế độ "Sửa".
     */
    private void handleEditableAuction(Auction auction) {

        selectedAuction = auction;
        hienThiChiTietSanPham(auction);
        showDetailForm();
        setupEditButton();
        maybeAddNotification(auction);
    }

    private void handleGeneralCancelledAuction(Auction auction) {
        if (auction.getStatus() == AuctionStatus.CANCELLED){
            setMessageCancelledAuction(auction);
        } else if (auction.getStatus() == AuctionStatus.CANCELLED_BY_ADMIN){
            setMessageCancelledByAdminAuction(auction);
        }
        selectedAuction = auction;
        hienThiChiTietSanPham(auction);
        showDetailForm();
        message.setVisible(true);
        message.setManaged(true);
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        maybeAddNotification(auction);
    }

    /** Chuyển UI sang chế độ hiển thị form chi tiết (ẩn panel winner). */
    private void showDetailForm() {
        if (winnerInfoBox != null) {
            winnerInfoBox.setVisible(false);
            winnerInfoBox.setManaged(false);
        }
        if (detailFormBox != null) {
            detailFormBox.setVisible(true);
            detailFormBox.setManaged(true);
        }
    }

    /** Thiết lập nút Save sang chế độ "Sửa". */
    private void setupEditButton() {
        message.setVisible(false);
        message.setManaged(false);
        saveButton.setVisible(true);
        saveButton.setManaged(true);
        saveButton.setStyle(
            "-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 5; -fx-padding: 5 0; -fx-font-size: 12px;");
        saveButton.setText("Sửa");
        saveButton.setOnAction(event -> {
            try {
                suaSp(event);
            } catch (Exception e) {
                log.error("Lỗi sửa sp: {}", e.getMessage(), e);
            }
        });
    }

    /** Thêm notification nếu phiên đã ở trạng thái cần thông báo (CLOSED/PAID/CANCELLED). */
    private void maybeAddNotification(Auction auction) {
        AuctionStatus st = auction.getStatus();
        if (st != AuctionStatus.CLOSED && st != AuctionStatus.PAID && st != AuctionStatus.CANCELLED)
            return;

        SellerNotification.Type type = switch (st) {
            case CLOSED -> SellerNotification.Type.CLOSED;
            case PAID -> SellerNotification.Type.PAID;
            default -> SellerNotification.Type.CANCELLED;
        };
        String pName = auction.getItem() != null
            ? auction.getItem().getName()
            : String.valueOf(auction.getAuctionId());
        String wName = auction.getWinnerName() != null && !auction.getWinnerName().isBlank()
            ? auction.getWinnerName()
            : "Người thắng";
        addOrReplaceNotification(
            new SellerNotification(auction.getAuctionId(), type, pName, wName, auction.getWinningPrice()));
    }

    // ── Form & UI helpers ─────────────────────────────────────────────────────

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        tfstepPrice.clear();
        Platform.runLater(() -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
        });
        startHourSpinner.getValueFactory().setValue(0);
        startMinuteSpinner.getValueFactory().setValue(0);
        endHourSpinner.getValueFactory().setValue(0);
        endMinuteSpinner.getValueFactory().setValue(0);
        categoryComboBox.setValue(null);
        Image.setImage(null);
        productList.getSelectionModel().clearSelection();
        image = "";
        hideVbox(artFields);
        hideVbox(vehicleFields);
        hideVbox(electronicsFields);
        artTitleField.clear();
        artistField.clear();
        yearCreatedField.clear();
        licensePlateField.clear();
        vehicleYearField.clear();
        brandVehicleField.clear();
        colorField.clear();
        brandElecField.clear();
        modelField.clear();
        selectedAuction = null;
    }

    @FXML
    public void onAddButton(ActionEvent event) {
        showDetailForm();
        message.setVisible(false);
        message.setManaged(false);
        saveButton.setVisible(true);
        saveButton.setManaged(true);
        saveButton.setStyle(
            "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 5; -fx-padding: 5 0; -fx-font-size: 12px;");
        saveButton.setText("Lưu");
        saveButton.setOnAction(_ -> {
            try {
                onSaveButton(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clearForm();
    }

    public Auction getInfo() throws Exception {
        String rawPrice2 = priceField.getText().replaceAll("[^0-9.]", "");
        if (rawPrice2.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập giá khởi điểm.");
            return null;
        }
        BigDecimal initPrice = new BigDecimal(rawPrice2);

        String rawStep = tfstepPrice.getText().replaceAll("[^0-9.]", "");
        if (rawStep.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập bước giá.");
            return null;
        }
        BigDecimal stepPrice = new BigDecimal(rawStep);

        LocalDate ngayBD = startDatePicker.getValue();
        int startHour = (int) startHourSpinner.getValue();
        int startMinu = (int) startMinuteSpinner.getValue();

        LocalDate ngayKT = endDatePicker.getValue();
        int endHour = (int) endHourSpinner.getValue();
        int endMinu = (int) endMinuteSpinner.getValue();

        if (ngayBD == null) {
            showAlert("Lỗi", "Vui lòng chọn ngày bắt đầu.");
            return null;
        }
        if (ngayKT == null) {
            showAlert("Lỗi", "Vui lòng chọn ngày kết thúc.");
            return null;
        }

        LocalDateTime tGianBD = LocalDateTime.of(ngayBD, LocalTime.of(startHour, startMinu));
        LocalDateTime tGianKT = LocalDateTime.of(ngayKT, LocalTime.of(endHour, endMinu));

        if (tGianBD.isAfter(tGianKT)) {
            showAlert("Lỗi", "Thời gian bắt đầu không được sau thời gian kết thúc");
            throw new InvalidInputException("startTime", String.valueOf(tGianBD),
                "Thời gian bắt đầu không được sau thời gian kết thúc");
        }
        if (tGianBD.isBefore(LocalDateTime.now())) {
            showAlert("Lỗi", "Thời gian bắt đầu phải sau thời gian bây giờ");
            throw new InvalidInputException("startTime", String.valueOf(tGianBD),
                "Thời gian bắt đầu phải sau thời gian hiện tại");
        }

        int sellerId = ClientModel.getInstance().getCurrentUser().getId();
        int itemId = (selectedAuction == null) ? 0 : selectedAuction.getItem().getItemId();

        String category = categoryComboBox.getValue();
        if (category == null) {
            showAlert("Lỗi", "Vui lòng chọn danh mục sản phẩm!", "Wait.gif");
            return null;
        }

        ItemFactory factory = switch (category) {
            case "ART" -> new ArtFactory();
            case "VEHICLE" -> new VehicleFactory();
            case "ELECTRONICS" -> new ElectronicsFactory();
            default -> new OtherItemFactory();
        };
        Item item = factory.createItem(collectFormData());
        item.setItemId(itemId);
        return new Auction(item, sellerId, initPrice, stepPrice, tGianBD, tGianKT, AuctionStatus.NOT_START);
    }

    private Map<String, String> collectFormData() {
        Map<String, String> data = new HashMap<>();
        int sellerId = ClientModel.getInstance().getCurrentUser().getId();
        String sellerName = ClientModel.getInstance().getCurrentUser().getName();

        data.put("sellerId", String.valueOf(sellerId));
        data.put("sellerName", sellerName);
        data.put("name", nameField.getText().trim());
        data.put("description", descriptionField.getText().trim());
        data.put("image", this.image);

        String category = categoryComboBox.getValue();
        if ("ART".equals(category)) {
            data.put("title", artTitleField.getText().trim());
            data.put("artist", artistField.getText().trim());
            data.put("yearCreated", yearCreatedField.getText().trim());
        } else if ("ELECTRONICS".equals(category)) {
            data.put("brand", brandElecField.getText().trim());
            data.put("model", modelField.getText().trim());
        } else if ("VEHICLE".equals(category)) {
            data.put("brand", brandVehicleField.getText().trim());
            data.put("licensePlate", licensePlateField.getText().trim());
            data.put("year", vehicleYearField.getText().trim());
            data.put("color", colorField.getText().trim());
        }
        return data;
    }

    // ── Network actions ───────────────────────────────────────────────────────

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
        if (selectedAuction.getStatus() == AuctionStatus.CANCELLED_BY_ADMIN) {
            showAlert("Không thể sửa", "Sản phẩm này đã bị admin hủy, không thể chỉnh sửa.", "Wrong.gif");
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
            if (Image != null) {
                Image.setImage(new Image(new ByteArrayInputStream(fileContent)));
            }
        } catch (Exception e) {
            log.error("Không thể tải ảnh lên: {}", e.getMessage(), e);
            showAlert("Lỗi", "Không thể tải ảnh lên!", "False.gif");
        }
    }

    // ── Winner info panel ─────────────────────────────────────────────────────

    /**
     * Hiển thị panel thông tin người thắng, ẩn form chi tiết.
     * Dùng cho cả CLOSED (có winner) và PAID.
     */
    private void showWinnerInfo(Auction auction) {
        winnerProductName.setText(auction.getItem() != null ? auction.getItem().getName() : "—");

        BigDecimal wPrice = auction.getWinningPrice();
        String priceStr = (wPrice != null)
            ? String.format("%,.0f VNĐ", wPrice)
            : String.format("%,.0f VNĐ", auction.getCurrentPrice());
        winnerFinalPrice.setText(priceStr);
        winnerBidPrice.setText(priceStr);

        winnerName.setText(auction.getWinnerName());

        if (winnerEmail != null)
            winnerEmail.setText(nullOrEmpty(auction.getWinnerEmail()) ? "—" : auction.getWinnerEmail());
        if (winnerSdt != null)
            winnerSdt.setText(nullOrEmpty(auction.getWinnerSdt()) ? "—" : auction.getWinnerSdt());

        // Ẩn form, hiện panel winner
        if (detailFormBox != null) {
            detailFormBox.setVisible(false);
            detailFormBox.setManaged(false);
        }
        if (winnerInfoBox != null) {
            winnerInfoBox.setVisible(true);
            winnerInfoBox.setManaged(true);
        }
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @FXML
    public void onBackToForm() {
        if (winnerInfoBox != null) {
            winnerInfoBox.setVisible(false);
            winnerInfoBox.setManaged(false);
        }
        if (detailFormBox != null) {
            detailFormBox.setVisible(true);
            detailFormBox.setManaged(true);
        }
        productList.getSelectionModel().clearSelection();
        selectedAuction = null;
    }

    // ── Detail form population ────────────────────────────────────────────────

    public void hienThiChiTietSanPham(Auction auction) {
        nameField.setText(auction.getItem().getName());
        descriptionField.setText(auction.getItem().getDescription());
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

        Item item = auction.getItem();
        String base64 = item.getImage();
        if (base64 != null && !base64.isEmpty()) {
            byte[] imgBytes = Base64.getDecoder().decode(base64);
            Image.setImage(new Image(new ByteArrayInputStream(imgBytes)));
        }

        ItemCategory cate = item.getCategory();
        categoryComboBox.getSelectionModel().select(String.valueOf(cate));
        showCategoryFields(String.valueOf(cate));
        populateCategoryFields(item, cate);
    }

    /** Điền các field đặc thù theo danh mục sản phẩm. */
    private void populateCategoryFields(Item item, ItemCategory cate) {
        if (cate == ItemCategory.ELECTRONICS) {
            if (item instanceof Electronics e) {
                brandElecField.setText(e.getBrand());
                modelField.setText(e.getModel());
            } else {
                brandElecField.clear();
                modelField.clear();
            }
        } else if (cate == ItemCategory.ART) {
            if (item instanceof Art a) {
                artTitleField.setText(a.getTitle());
                artistField.setText(a.getArtist());
                yearCreatedField.setText(String.valueOf(a.getYearCreated()));
            } else {
                artTitleField.clear();
                artistField.clear();
                yearCreatedField.clear();
            }
        } else if (cate == ItemCategory.VEHICLE) {
            if (item instanceof Vehicle v) {
                licensePlateField.setText(v.getLicensePlate());
                vehicleYearField.setText(String.valueOf(v.getYear()));
                brandVehicleField.setText(v.getBrand());
                colorField.setText(v.getColor());
            } else {
                licensePlateField.clear();
                vehicleYearField.clear();
                brandVehicleField.clear();
                colorField.clear();
            }
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    private void applyFilter(String filter) {
        if (filteredList == null) return;
        filteredList.setPredicate(a -> {
            if (filter == null || filter.equals("Tất cả")) return true;
            AuctionStatus st = a.getStatus();
            boolean hasWinner = a.getWinnerName() != null && !a.getWinnerName().isBlank();
            return switch (filter) {
                case "Chưa bắt đầu" -> st == AuctionStatus.NOT_START;
                case "Đang diễn ra" -> st == AuctionStatus.RUNNING;
                case "Chờ thanh toán" -> st == AuctionStatus.CLOSED && hasWinner;
                case "Thành toán thành công" -> st == AuctionStatus.PAID;
                case "Sản phẩm đã hủy" -> st == AuctionStatus.CANCELLED || st == AuctionStatus.CANCELLED_BY_ADMIN;
                case "Không ai đấu giá" -> st == AuctionStatus.CLOSED && !hasWinner;
                default -> true;
            };
        });
    }

    // ── Category UI helpers ───────────────────────────────────────────────────

    public void hideVbox(VBox vbox) {
        vbox.setManaged(false);
        vbox.setVisible(false);
    }

    public void showVbox(VBox vbox) {
        vbox.setVisible(true);
        vbox.setManaged(true);
    }

    public void showCategoryFields(String category) {
        hideVbox(artFields);
        hideVbox(vehicleFields);
        hideVbox(electronicsFields);
        switch (category) {
            case "ART" -> showVbox(artFields);
            case "VEHICLE" -> showVbox(vehicleFields);
            case "ELECTRONICS" -> showVbox(electronicsFields);
        }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    private void addOrReplaceNotification(SellerNotification notif) {
        SellerNotification existing = notifications.stream()
            .filter(n -> n.getAuctionId() == notif.getAuctionId())
            .findFirst()
            .orElse(null);

        if (existing == null) {
            if (ClientModel.getInstance().isNotificationRead(notif.getNotificationId())) {
                notif.setRead(true);
            }
            notifications.add(0, notif);
        } else if (priority(notif.getType()) > priority(existing.getType())) {
            // Upgrade type → sự kiện mới → đánh dấu chưa đọc
            notif.setRead(false);
            ClientModel.getInstance().markNotificationUnread(notif.getAuctionId());
            notifications.remove(existing);
            notifications.add(0, notif);
        }
    }

    private static int priority(SellerNotification.Type type) {
        return switch (type) {
            case PAID -> 2;
            case CANCELLED -> 1;
            case CLOSED -> 0;
            case CANCELLED_BY_ADMIN -> 3;
        };
    }

    private void buildNotificationPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/javfxtutorial/hethongdaugia/view/fxml/NotifiCationPopup.fxml"));
            VBox popupRoot = loader.load();
            popupController = loader.getController();
            popupController.setOnMarkRead(notif -> Platform.runLater(this::updateBadge));
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

        if (cmdClass == AddAuctionCommand.class) {
            handleAddAuctionResponse(rp);
        } else if (cmdClass == GetAuctionsBySellerIdCommand.class) {
            handleLoadAuctionsResponse(rp);
        } else if (cmdClass == DeleteAuctionCommand.class) {
            handleDeleteAuctionResponse(rp);
        } else if (cmdClass == UpdateAuctionCommand.class) {
            handleUpdateAuctionResponse(rp);
        } else if (cmdClass == UpdateAuctionStatusCommand.class) {
            handleAuctionStatusPushResponse(rp);
        }
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

    /** Đồng bộ danh sách notification từ danh sách phiên đấu giá đã load. */
    private void syncNotificationsFromAuctions(List<Auction> auctions) {
        for (Auction a : auctions) {
            AuctionStatus st = a.getStatus();
            if (st != AuctionStatus.CLOSED && st != AuctionStatus.PAID
                && st != AuctionStatus.CANCELLED && st != AuctionStatus.CANCELLED_BY_ADMIN)
                continue;

            SellerNotification.Type type = switch (st) {
                case CLOSED -> SellerNotification.Type.CLOSED;
                case PAID -> SellerNotification.Type.PAID;
                case CANCELLED_BY_ADMIN -> SellerNotification.Type.CANCELLED_BY_ADMIN;
                default -> SellerNotification.Type.CANCELLED;
            };
            String pName = a.getItem() != null ? a.getItem().getName() : String.valueOf(a.getAuctionId());
            String wName = a.getWinnerName() != null ? a.getWinnerName() : "N/A";

            SellerNotification notif = new SellerNotification(a.getAuctionId(), type, pName, wName, a.getWinningPrice());
            if (a.getEndingTime() != null) notif.setCreatedAt(a.getEndingTime());
            if (ClientModel.getInstance().isNotificationReadByAuction(a.getAuctionId())) notif.setRead(true);

            Optional<SellerNotification> existing = notifications.stream()
                .filter(n -> n.getAuctionId() == a.getAuctionId())
                .findFirst();

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
        // Server push: admin xóa sản phẩm
        if ("ADMIN_DELETED_PRODUCT".equals(rp.getMessage()) && rp.getPayLoad() instanceof SellerNotification adminNotif) {
            Platform.runLater(() -> {
                observable.removeIf(a -> a.getAuctionId() == adminNotif.getAuctionId());
                addOrReplaceNotification(adminNotif);
            });
            return;
        }

        // Response bình thường cho lệnh xóa của seller
        NetworkManager.getInstance().unregister(DeleteAuctionCommand.class, this);
        Platform.runLater(() -> {
            if (rp.isSuccess()) {
                Auction deleted = (rp.getPayLoad() instanceof Auction)
                    ? (Auction) rp.getPayLoad()
                    : selectedAuction;
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
        // Server push: admin hủy phiên
        if ("ADMIN_CANCELLED_AUCTION".equals(rp.getMessage()) && rp.getPayLoad() instanceof SellerNotification adminNotif) {
            Platform.runLater(() -> {
                for (int i = 0; i < observable.size(); i++) {
                    if (observable.get(i).getAuctionId() == adminNotif.getAuctionId()) {
                        observable.get(i).setStatus(AuctionStatus.CANCELLED_BY_ADMIN);
                        observable.set(i, observable.get(i)); // trigger refresh
                        break;
                    }
                }
                addOrReplaceNotification(adminNotif);
            });
            return;
        }

        // Server push: cập nhật trạng thái phiên thông thường
        if (!(rp.getPayLoad() instanceof Auction auction)) return;
        AuctionStatus status = auction.getStatus();
        if (status != AuctionStatus.CLOSED && status != AuctionStatus.PAID
            && status != AuctionStatus.CANCELLED && status != AuctionStatus.CANCELLED_BY_ADMIN)
            return;

        SellerNotification.Type type = switch (status) {
            case CLOSED -> SellerNotification.Type.CLOSED;
            case PAID -> SellerNotification.Type.PAID;
            case CANCELLED_BY_ADMIN -> SellerNotification.Type.CANCELLED_BY_ADMIN;
            default -> SellerNotification.Type.CANCELLED;
        };
        String productName = auction.getItem() != null
            ? auction.getItem().getName()
            : String.valueOf(auction.getAuctionId());
        String winnerNameStr = auction.getWinnerName() != null ? auction.getWinnerName() : "N/A";
        SellerNotification notif = new SellerNotification(
            auction.getAuctionId(), type, productName, winnerNameStr, auction.getWinningPrice());

        Platform.runLater(() -> {
            addOrReplaceNotification(notif);
            // Cập nhật auction trong danh sách hiển thị
            for (int i = 0; i < observable.size(); i++) {
                if (observable.get(i).getAuctionId() == auction.getAuctionId()) {
                    observable.set(i, auction);
                    break;
                }
            }
        });
    }
}