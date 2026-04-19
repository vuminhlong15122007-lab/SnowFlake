package com.javfxtutorial.hethongdaugia.common.model;

public class Vehicle extends Item {
    private String licensePlate;
    private int year;
    private String brand;
    private String color;

//    @Override
    public String getCategory() {
        return "Vehicle";
    }
}