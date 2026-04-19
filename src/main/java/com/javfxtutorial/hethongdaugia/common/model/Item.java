package com.javfxtutorial.hethongdaugia.common.model;
import java.io.Serializable;

public class Item implements Serializable {
    private int sellerId;
    private int itemId;
    private String name;
    private String description;
    private String image; // ảnh đã được mã hóa
    private String sellerName;

    public Item(int sellerId, String name, String description, String image, String sellerName) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.image = image;
        this.sellerName = sellerName;
    }

    public Item(int itemId, int sellerId, String name, String description, String image, String sellerName) {
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.image = image;
        this.sellerName = sellerName;
    }

    public Item(int sellerId, int itemId, String name, String description, String image) {
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public Item() {}

    public void setItemId(int itemId) {this.itemId = itemId;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getImage() {return image;}
    public void setImage(String imagePath) {this.image = imagePath;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public int getItemId() {return itemId;}
    public int getSellerId() {return sellerId;}
    public void setSellerId(int sellerId) {this.sellerId = sellerId;}
    public String getSellerName() {return sellerName;}
    public void setSellerName(String sellerName) {this.sellerName = sellerName;}
//    public abstract  String getCategory();         // "Electronics" / "Art" / "Vehicle"

}


