package com.javfxtutorial.hethongdaugia.common.model;
import java.io.Serializable;
import java.time.LocalDate;

public class Item implements Serializable {
    private int sellerId;
    private String itemId;
    private String name;
    private String description;
    private String imagePath; // đường dẫn ảnh
    private double currentPrice;
    private double stepPrice;

    public Item(int sellerId, String itemId, String name, String description, String imagePath, double currentPrice, double stepPrice) {
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.currentPrice = currentPrice;
        this.stepPrice = stepPrice;
    }

    public double getStepPrice() {return stepPrice;}
    public void setStepPrice(double stepPrice) {this.stepPrice = stepPrice;}
    public double getCurrentPrice() {return currentPrice;}
    public void setCurrentPrice(double currrntPrice) {this.currentPrice = currrntPrice;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getImagePath() {return imagePath;}
    public void setImagePath(String imagePath) {this.imagePath = imagePath;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public String getItemId() {return itemId;}
    public int getSellerId() {return sellerId;}
    public void setSellerId(int sellerId) {this.sellerId = sellerId;}
}


