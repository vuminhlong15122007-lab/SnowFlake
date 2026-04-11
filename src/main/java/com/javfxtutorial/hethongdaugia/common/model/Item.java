package com.javfxtutorial.hethongdaugia.common.model;
import java.io.Serializable;
import java.time.LocalDate;

public class Item implements Serializable {
    private int sellerId;
    private int itemId;
    private String name;
    private String description;
    private String imagePath; // đường dẫn ảnh
    private double giaHienTai;

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Item(int sellerId, int itemId, String name, String description, double giaHienTai, String imagePath) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.giaHienTai = giaHienTai;
    }

    public Item(int sellerId, int itemId, String name, String description, String imagePath, double giaHienTai) {
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.giaHienTai = giaHienTai;
    }

    public double getGiaHienTai() {
        return giaHienTai;
    }

    public void setGiaHienTai(double giaHienTai) {
        this.giaHienTai = giaHienTai;
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
    public int getItemId() {return itemId;}
    public int getSellerId() {return sellerId;}
    public void setSellerId(int sellerId) {this.sellerId = sellerId;}
}


