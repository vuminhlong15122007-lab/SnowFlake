package com.javfxtutorial.hethongdaugia.common.model;

public class Item {
    private int sellerId;
    private String itemId;
    private String name;
    private String description;
    private String imagePath; // đường dẫn ảnh

    public Item(int sellerId, String itemId, String name, String description, String imagePath) {
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public String getItemId() {return itemId;}
    public int getSellerId() {return sellerId;}
    public void setSellerId(int sellerId) {this.sellerId = sellerId;}
}


