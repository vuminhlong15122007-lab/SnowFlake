package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import javafx.fxml.FXML;

public class ElectronicsFactory extends ItemFactory {

  private Item baseItem;
  @FXML private String brandElec, model;

  public ElectronicsFactory(Item baseItem, String brandElec, String model) {
    this.baseItem = baseItem;
    this.brandElec = brandElec;
    this.model = model;
  }

  @Override
  public Item createItemFromForm() {
    String sellerName = baseItem.getSellerName();
    int sellerId = baseItem.getSellerId();
    int itemId = baseItem.getItemId();
    String name = baseItem.getName();
    String description = baseItem.getDescription();
    String image = baseItem.getImage();
    return new Electronics(
        sellerName, sellerId, itemId, name, description, image, brandElec, model);
  }
}
