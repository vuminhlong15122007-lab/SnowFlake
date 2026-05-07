package com.javfxtutorial.hethongdaugia.common.model;

public class Vehicle extends Item {
    private String licensePlate;
    private int year;
    private String brand;
    private String color;

    public Vehicle(int sellerId, String name, String description, String image, String sellerName, String licensePlate, int year, String brand, String color) {
        super(sellerId, name, description, image, sellerName);
        this.licensePlate = licensePlate;
        this.year = year;
        this.brand = brand;
        this.color = color;
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