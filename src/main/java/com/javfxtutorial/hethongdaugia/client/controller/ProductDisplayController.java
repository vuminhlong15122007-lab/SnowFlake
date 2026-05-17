package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.*;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class ProductDisplayController {
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


    private final Item item = ClientModel.getInstance().getCurrentItem();
    private final Auction auction = ClientModel.getInstance().getCurrentAuction();

    public void setData() { // nhan du lieu tu man Item..
        LbMotasp.setText(item.getDescription());
        StartTimeLabel.setText(String.valueOf(auction.getStartingTime()));
        EndingtimeLabel.setText(String.valueOf(auction.getEndingTime()));
        lbTenngban.setText(item.getSellerName());
        ItemNameLabel.setText(item.getName());
        ItemPriceLabel.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        lbTenngban.setText(auction.getItem().getSellerName());
        stepPriceLabel.setText(String.format("%,.0f VND", auction.getStepPrice()));
        initPriceLabel.setText(String.format("%,.0f VND", auction.getInitPrice()));

        // Loại sản phẩm
        ItemCategory category = item.getCategory();
        if (category != null) {
            lbLoaisp.setText(category.name());
        } else {
            lbLoaisp.setText("Không xác định");
        }

        // Ảnh
        String base64Data = auction.getItem().getImage();
        ImageHelper.loadBase64ToImageView(itemImageView,base64Data);
    }

    @FXML
    public void initialize() {
        setData();
        showCategoryInfo();
        if (auction == null) return;
        if (auction.getStatus() == AuctionStatus.RUNNING) {
            TimeLeft timer = new TimeLeft(lbtimeLeft, auction.getEndingTime());
            timer.start();
        } else if (auction.getStatus() == AuctionStatus.NOT_START) {
            lbtimeLeft.setText("CHƯA BẮT ĐẦU");
            UI01.setStyle("-fx-text-fill: -sf-warning; -fx-alignment: CENTER;");
            UI02.setStyle("-fx-background-color: -sf-surface; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: -sf-warning; -fx-alignment: CENTER;");
            lbtimeLeft.setStyle("-fx-text-fill: -sf-warning;");
            ThamGiaDauGiaBtn.setText("Chưa thể tham gia");
            ThamGiaDauGiaBtn.setStyle("-fx-background-color: linear-gradient(to right, -sf-danger, -sf-warning); -fx-text-fill: -sf-on-accent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25;");
        } else {
            lbtimeLeft.setText("ĐÃ KẾT THÚC");
            UI01.setStyle("-fx-text-fill: -sf-danger; -fx-alignment: CENTER;");
            UI02.setStyle("-fx-background-color: -sf-surface; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: -sf-danger; -fx-alignment: CENTER;");
            lbtimeLeft.setStyle("-fx-text-fill: -sf-danger;");
            ThamGiaDauGiaBtn.setText("Phiên đấu giá đã đóng");
            ThamGiaDauGiaBtn.setStyle("-fx-background-color: -sf-neutral; -fx-text-fill: -sf-on-accent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25;");
        }
    }


    @FXML
    public void QuaylaiMenu(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
    }


    @FXML
    public void goToManHinhDauGiaTrucTiep(ActionEvent event) {
        if (auction.getStatus() == AuctionStatus.RUNNING) {
            System.out.println("Phiên đấu giá hiện tại: " + ClientModel.getInstance().getCurrentAuction());
            changeScene(event , "/com/javfxtutorial/hethongdaugia/view/fxml/LiveAuction.fxml");

        } else {
            if (auction.getStatus() == AuctionStatus.CLOSED){
                showAlert("Không thể vào phiên đấu giá", "Đã hết phiên đấu giá");}
            else if (auction.getStatus() == AuctionStatus.NOT_START){
                showAlert("Không thể vào phiên đấu giá", "Chưa bắt đầu phiên đấu giá");}
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
}

