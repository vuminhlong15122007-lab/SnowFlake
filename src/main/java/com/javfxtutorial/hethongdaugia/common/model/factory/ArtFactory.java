package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Art;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ArtFactory extends ItemFactory {
  private Item baseItem;
  private String artTitle, artist;
  private int  yearCreated;

  public ArtFactory(Item baseItem, String artTitle, String artist, int yearCreated) {
    this.baseItem = baseItem;
    this.artTitle = artTitle;
    this.artist = artist;
    this.yearCreated = yearCreated;
  }



  @Override
  public Item createItemFromForm() {
    String sellerName = baseItem.getSellerName();
    int sellerId = baseItem.getSellerId();
    int itemId = baseItem.getItemId();
    String name = baseItem.getName();
    String description = baseItem.getDescription();
    String image = baseItem.getImage();
    return new Art(
        sellerName, sellerId, itemId, name, description, image, artist, yearCreated, artTitle);
  }
}
