package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class AuctionSessionController {
    @FXML private Label AuctionStatusText;
    @FXML private Label ItemNameLabel;
    @FXML private Label ItemPriceText;
    @FXML private Label SellerNameText;
    @FXML private Label StartTimeText;
    @FXML private ImageView Image;

    private Item item;
    private Auction auction;

    public void setData(Auction auction) throws IOException, ClassNotFoundException {
        if (auction == null || auction.getAuctionId() <= 0 || auction.getItem() == null) {
            return;
        }

        this.auction = auction;
        this.item = auction.getItem();

        ItemNameLabel.setText(item.getName());
        SellerNameText.setText(item.getSellerName());
        StartTimeText.setText(String.valueOf(auction.getStartingTime()));
        ItemPriceText.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        AuctionStatusText.setText(String.valueOf(auction.getStatus()));
        Image.setImage(loadImage(item.getImage()));
    }

    private Image loadImage(String imageData) {
        if (imageData == null || imageData.isBlank()) {
            return null;
        }

        String trimmed = imageData.trim();

        try {
            String normalized = stripDataUriPrefix(trimmed);
            byte[] imageBytes = Base64.getDecoder().decode(normalized);
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            if (!image.isError()) {
                return image;
            }
        } catch (IllegalArgumentException ignored) {
        }

        String resourcePath = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }

            Image image = new Image(inputStream);
            return image.isError() ? null : image;
        } catch (IOException ignored) {
            return null;
        }
    }

    private String stripDataUriPrefix(String imageData) {
        int separatorIndex = imageData.indexOf(',');
        if (imageData.startsWith("data:") && separatorIndex >= 0) {
            return imageData.substring(separatorIndex + 1);
        }
        return imageData;
    }

    @FXML
    public void clickToLiveAuction(ActionEvent event) {
        try {
            ClientModel.getInstance().setCurrentItem(item);
            ClientModel.getInstance().setCurrentAuction(auction);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/fxml/man_hinh_hien_thi_sp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
