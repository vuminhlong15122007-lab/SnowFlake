package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.*;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDisplayController implements ResponseListener {

    @FXML
    private Label EndingtimeLabel;
    @FXML
    private Label ItemNameLabel;
    @FXML
    private Label ItemPriceLabel;
    @FXML
    private Label LbMotasp;
    @FXML
    private Label StartTimeLabel;
    @FXML
    private ImageView itemImageView;
    @FXML
    private Label lbTenngban;
    @FXML
    private Label lbtimeLeft;
    @FXML
    private Label UI01;
    @FXML
    private VBox UI02;
    @FXML
    private Button ThamGiaDauGiaBtn;
    @FXML
    private Label lbLoaisp;
    @FXML
    private VBox artInfoBox, vehicleInfoBox, electronicsInfoBox;
    @FXML
    private Label artTitleValue, artistValue, yearCreatedValue;
    @FXML
    private Label licensePlateValue, vehicleYearValue, brandValue, colorValue;
    @FXML
    private Label elecBrandValue, modelValue;
    @FXML
    private Label initPriceLabel, stepPriceLabel;
    @FXML
    private Label detailTitle;

    private final Item item = ClientModel.getInstance().getCurrentAuction().getItem();
    private final Auction auction = ClientModel.getInstance().getCurrentAuction();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final Logger log = LoggerFactory.getLogger(ProductDisplayController.class);

    private final NetworkManager networkManager = NetworkManager.getInstance();
    private TimeLeft timer;
    private ScheduledExecutorService statusRefreshScheduler;


    public void setData() {
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
    }


    @FXML
    public void initialize() {
        setData();
        showCategoryInfo();
        auction.statusProperty().addListener(((_, _, newVal) -> {
            updateUI(newVal);
        })); // thay đổi UI nếu có status mơid

        // Register lắng nghe bid mới CHỈ khi đang RUNNING — 1 lần duy nhất
        if (auction.getStatus() == AuctionStatus.RUNNING) {
            networkManager.register(PlaceBidCommand.class, this);
        }
        updateUI(auction.getStatus());

        if (timer == null) return;
        timer.setOnFinished(() -> {
            if (auction.getStatus() == AuctionStatus.NOT_START) { // hết countdown running → chuyển CLOSED
                auction.setStatus(AuctionStatus.RUNNING);
                Platform.runLater(
                        () -> {
                            try {
                                Command cmd = new UpdateAuctionStatusCommand(auction);
                                NetworkManager.getInstance().sendRequest(cmd, this);
                            } catch (ConnectionFailedException e) {
                                log.error("Không kết nối được server");
                                showAlert("Lỗi", e.getMessage());
                            } catch (SendFailedException e) {
                                log.error("Không gửi được command");
                                showAlert("Lỗi", e.getMessage());
                            }
                        });
            } else if (auction.getStatus() == AuctionStatus.RUNNING) { // hết countdown chờ thanh toán → CANCELLED
                auction.setStatus(AuctionStatus.CLOSED);
                Platform.runLater(
                        () -> {
                            try {
                                Command cmd = new UpdateAuctionStatusCommand(auction);
                                NetworkManager.getInstance().sendRequest(cmd, this);
                            } catch (ConnectionFailedException e) {
                                log.error("Không kết nối được server");
                                showAlert("Lỗi", e.getMessage());
                            } catch (SendFailedException e) {
                                log.error("Không gửi được command");
                                showAlert("Lỗi", e.getMessage());
                            }
                        });
            }
        });
        timer.start();
    }


    private void updateUI(AuctionStatus status) {
        switch (status) {
            case RUNNING -> {

                ThamGiaDauGiaBtn.setText("Tham gia");
                timer = new TimeLeft(lbtimeLeft, auction.getEndingTime());


            }
            case NOT_START -> {
                UI01.setStyle("-fx-text-fill: -sf-warning; -fx-alignment: CENTER;");
                UI02.setStyle("-fx-background-color: -sf-surface; -fx-background-radius: 10; "
                        + "-fx-border-radius: 10; -fx-border-color: -sf-warning; -fx-alignment: CENTER;");
                lbtimeLeft.setStyle("-fx-text-fill: -sf-warning;");
                ThamGiaDauGiaBtn.setText("Chưa thể tham gia");
                ThamGiaDauGiaBtn.setStyle("-fx-background-color: linear-gradient(to right, -sf-danger, -sf-warning); "
                        + "-fx-text-fill: -sf-on-accent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25;");
                timer = new TimeLeft(lbtimeLeft, auction.getStartingTime());

            }
            default -> { // CLOSED
                lbtimeLeft.setText("ĐÃ KẾT THÚC");
                UI01.setStyle("-fx-text-fill: -sf-danger; -fx-alignment: CENTER;");
                UI02.setStyle("-fx-background-color: -sf-surface; -fx-background-radius: 10; "
                        + "-fx-border-radius: 10; -fx-border-color: -sf-danger; -fx-alignment: CENTER;");
                lbtimeLeft.setStyle("-fx-text-fill: -sf-danger;");
                ThamGiaDauGiaBtn.setText("Phiên đấu giá đã đóng");
                ThamGiaDauGiaBtn.setStyle("-fx-background-color: -sf-neutral; -fx-text-fill: -sf-on-accent; "
                        + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25;");
            }
        }
    }


    @FXML
    public void QuaylaiMenu(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
    }

    @FXML
    public void goToManHinhDauGiaTrucTiep(ActionEvent event) {
        AccountType type = ClientModel.getInstance().getCurrentUser().getAccountType();
        if (type == AccountType.ADMIN) {
            showAlert("KHông thể tham gia", "Bạn không thể tham gia phin đấu giá");
            return;
        }
        if (auction.getStatus() == AuctionStatus.RUNNING) {
            changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/LiveAuction.fxml");
        } else {
            if (auction.getStatus() == AuctionStatus.CLOSED) {
                showAlert("Không thể vào phiên đấu giá", "Đã hết phiên đấu giá");
            } else if (auction.getStatus() == AuctionStatus.NOT_START) {
                showAlert("Không thể vào phiên đấu giá", "Chưa bắt đầu phiên đấu giá");
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
            if (artTitleValue != null) artTitleValue.setText(art.getTitle() != null ? art.getTitle() : "...");
            if (artistValue != null) artistValue.setText(art.getArtist() != null ? art.getArtist() : "...");
            if (yearCreatedValue != null) yearCreatedValue.setText(String.valueOf(art.getYearCreated()));
        } else if (item instanceof Vehicle vehicle) {
            showBox(vehicleInfoBox);
            if (detailTitle != null) detailTitle.setText("THÔNG TIN VEHICLE");
            if (licensePlateValue != null) licensePlateValue.setText(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "...");
            if (vehicleYearValue != null) vehicleYearValue.setText(vehicle.getYear() > 0 ? String.valueOf(vehicle.getYear()) : "...");
            if (brandValue != null) brandValue.setText(vehicle.getBrand() != null ? vehicle.getBrand() : "...");
            if (colorValue != null) colorValue.setText(vehicle.getColor() != null ? vehicle.getColor() : "...");
        } else if (item instanceof Electronics elec) {
            showBox(electronicsInfoBox);
            if (detailTitle != null) detailTitle.setText("THÔNG TIN ELECTRONICS");
            if (elecBrandValue != null) elecBrandValue.setText(elec.getBrand() != null ? elec.getBrand() : "...");
            if (modelValue != null) modelValue.setText(elec.getModel() != null ? elec.getModel() : "...");
        }
    }

    private void hideBox(VBox box) {
        if (box != null) { box.setVisible(false); box.setManaged(false); }
    }

    private void showBox(VBox box) {
        if (box != null) { box.setVisible(true); box.setManaged(true); }
    }

    @Override
    public void onResponse(Response rp) {

    }
}