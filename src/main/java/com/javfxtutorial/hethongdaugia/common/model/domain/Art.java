package com.javfxtutorial.hethongdaugia.common.model.domain;

import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;

public class Art extends Item {
  private String artist;
  private int yearCreated;
  private String title;

  public Art(
      String sellerName,
      int sellerId,
      int itemId,
      String name,
      String description,
      String image,
      String artist,
      int yearCreated,
      String title) {
    super(sellerName, sellerId, itemId, name, description, image, ItemCategory.ART);
    this.artist = artist;
    this.yearCreated = yearCreated;
    this.title = title;
  }

  public Art() {
    super();
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public int getYearCreated() {
    return yearCreated;
  }

  public void setYearCreated(int yearCreated) {
    this.yearCreated = yearCreated;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
