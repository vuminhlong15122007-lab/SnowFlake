package com.javfxtutorial.hethongdaugia.common.model;

public class Electronics extends Item{
    private String brand;       // hãng sản xuất
    private String model;       // Tên dòng máy

    public Electronics(int sellerId, String name, String description, String image, String sellerName, String brand, String model) {
        super(sellerId, name, description, image, sellerName);
        this.brand = brand;
        this.model = model;
    }

    public Electronics() { super(); }
    public String getBrand() {return brand;}
    public void setBrand(String brand) {this.brand = brand;}
    public String getModel() {return model;}
    public void setModel(String model) {this.model = model;}
}

