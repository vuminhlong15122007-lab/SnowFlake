package com.javfxtutorial.hethongdaugia.common.model;

import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;

public class Vehicle extends Item {
    private String licensePlate;
    private int year;
    private String brand;
    private String color;

    public Vehicle(String sellerName, int sellerId, int itemId, String name, String description, String image, String licensePlate, int year, String brand, String color) {
        super(sellerName, sellerId, itemId, name, description, image, ItemCategory.Vehicle);
        this.licensePlate = licensePlate;
        this.year = year;
        this.brand = brand;
        this.color = color;
    }
    // Thêm vào class Vehicle
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.setCategory(ItemCategory.Vehicle);
    }

    public Vehicle() {super();}

    public String getLicensePlate() {return licensePlate;}
    public int getYear() {return year;}
    public String getBrand() {return brand;}
    public String getColor() {return color;}
    public void setLicensePlate(String licensePlate) {this.licensePlate = licensePlate;}
    public void setYear(int year) {this.year = year;}
    public void setBrand(String brand) {this.brand = brand;}
    public void setColor(String color) {this.color = color;}

}