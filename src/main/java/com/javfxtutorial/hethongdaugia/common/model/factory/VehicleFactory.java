package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;

public class VehicleFactory extends ItemFactory{

    private int sellerId;
    private String name, description, image, sellerName;
    private String brand, color;
    private int year;
    private String licensePlate;

    public VehicleFactory(int sellerId, String name, String description, String image, String sellerName, String brand, String color, int year, String licensePlate) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.image = image;
        this.sellerName = sellerName;
        this.brand = brand;
        this.color = color;
        this.year = year;
        this.licensePlate = licensePlate;
    }

    public VehicleFactory() {}

    @Override
    public Item createItem() {
        return new Vehicle();
    }
}
