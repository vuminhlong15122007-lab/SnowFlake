package com.javfxtutorial.hethongdaugia.common.model;

import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;

public class Electronics extends Item{
    private String brand;       // hãng sản xuất
    private String model;       // Tên dòng máy

    public Electronics(String sellerName, int sellerId, int itemId, String name, String description, String image, String brand, String model) {
        super(sellerName, sellerId, itemId, name, description, image, ItemCategory.ELECTRONICS);
        this.brand = brand;
        this.model = model;
    }

    public Electronics() { super(); }
    public String getBrand() {return brand;}
    public void setBrand(String brand) {this.brand = brand;}
    public String getModel() {return model;}
    public void setModel(String model) {this.model = model;}
}

