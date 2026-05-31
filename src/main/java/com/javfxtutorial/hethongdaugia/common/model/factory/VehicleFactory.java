package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.domain.Vehicle;

public class VehicleFactory extends ItemFactory {
  private Item baseItem;
  private String licensePlate, brandVehicle, color;
  private int vehicleYear;

  public VehicleFactory(
      Item baseItem, String licensePlate, int vehicleYear, String brandVehicle, String color) {
    this.baseItem = baseItem;
    this.licensePlate = licensePlate;
    this.vehicleYear = vehicleYear;
    this.brandVehicle = brandVehicle;
    this.color = color;
  }

  @Override
  public Item createItemFromForm() {
    String sellerName = baseItem.getSellerName();
    int sellerId = baseItem.getSellerId();
    int itemId = baseItem.getItemId();
    String name = baseItem.getName();
    String description = baseItem.getDescription();
    String image = baseItem.getImage();
    return new Vehicle(
        sellerName,
        sellerId,
        itemId,
        name,
        description,
        image,
        licensePlate,
        vehicleYear,
        brandVehicle,
        color);
  }
}
