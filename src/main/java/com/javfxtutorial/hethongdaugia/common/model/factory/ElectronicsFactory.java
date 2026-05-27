package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ElectronicsFactory extends ItemFactory {

  private Item baseItem;
  @FXML private TextField brandElecField, modelField;

  public ElectronicsFactory(Item baseItem, TextField brandElecField, TextField modelField) {
    this.baseItem = baseItem;
    this.brandElecField = brandElecField;
    this.modelField = modelField;
  }

  @Override
  public void showData() {
    if (baseItem instanceof Electronics) {
      Electronics e = (Electronics) baseItem;
      brandElecField.setText(e.getBrand());
      modelField.setText(e.getModel());
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
    String brand = brandElecField.getText();
    String model = modelField.getText();
    return new Electronics(sellerName, sellerId, itemId, name, description, image, brand, model);
  }
}
