package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Art;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Map;

public class ArtFactory extends ItemFactory {
    private Item baseItem;
    @FXML private TextField artTitleField,artistField, yearCreatedField;

    public ArtFactory(Item baseItem, TextField artTitleField, TextField artistField, TextField yearCreatedField) {
      this.baseItem = baseItem;
        this.artTitleField = artTitleField;
        this.artistField = artistField;
        this.yearCreatedField = yearCreatedField;
    }

    @Override
    public void showData(){
        if (baseItem instanceof Art) {
            Art art = (Art) baseItem;
            artistField.setText(art.getArtist());
            artTitleField.setText(art.getTitle());
            yearCreatedField.setText(String.valueOf(art.getYearCreated()));
        }
    }

    @Override
    public Item createItemFromForm() {
        String sellerName = baseItem.getSellerName();
        int sellerId = baseItem.getSellerId();
        int itemId = baseItem.getItemId();
        String name = baseItem.getName();
        String description = baseItem.getDescription();
        String image = baseItem.getImage();
        String artist = artistField.getText();
        int yearCreated = Integer.parseInt(yearCreatedField.getText());
        String title = artTitleField.getText();
        return new Art( sellerName,
            sellerId,
            itemId,
            name,
            description,
            image,
            artist,
            yearCreated,
            title);
    }
}
