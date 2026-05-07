package com.javfxtutorial.hethongdaugia.common.model.Factory;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;

public class VehicleFactory extends ItemFactory{

    private int sellerId;
    private String name, description, image, sellerName;
    private String brand, color;
    private int year;
    private String licensePlate;

    public VehicleFactory(int sellerId, String name, String description, String image,
                          String sellerName,String licensePlate, String brand, int year, String color) {
        this.sellerId = sellerId; this.name = name;
        this.description = description; this.image = image;
        this.sellerName = sellerName;
        this.brand = brand; this.year = year; this.color = color;
        this.licensePlate = licensePlate;
    }

    public VehicleFactory() {}

    @Override
    public Item createItem() {
        return new Vehicle(sellerId, name, description,
                image, sellerName, licensePlate, year, brand, color);
    }
}
