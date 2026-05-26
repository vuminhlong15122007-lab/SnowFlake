package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.domain.Vehicle;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Map;

public class VehicleFactory extends ItemFactory {
    private Item baseItem;
    @FXML private TextField licensePlateField, vehicleYearField, brandVehicleField, colorField;

    public VehicleFactory(Item baseItem, TextField licensePlateField, TextField vehicleYearField, TextField brandVehicleField, TextField colorField) {
        this.baseItem = baseItem;
        this.licensePlateField = licensePlateField;
        this.vehicleYearField = vehicleYearField;
        this.brandVehicleField = brandVehicleField;
        this.colorField = colorField;
    }

    @Override
    public void showData(){
        if (baseItem instanceof Vehicle) {
            Vehicle v = (Vehicle) baseItem;
            licensePlateField.setText(v.getLicensePlate());
            vehicleYearField.setText(String.valueOf(v.getYear()));
            brandVehicleField.setText(v.getBrand());
            colorField.setText(v.getColor());
        }
    }

    @Override
    public Item createItemFromForm() {
        String sellerName = baseItem.getSellerName();
        int sellerId = baseItem.getSellerId();
        int itemId = baseItem.getItemId();
        String name = baseItem.getName();
        String description = baseItem.getDescription();
        String image = baseItem.getImage();
        String licensePlate = licensePlateField.getText();
        String brand = brandVehicleField.getText();
        String color = colorField.getText();
        int year = Integer.parseInt(vehicleYearField.getText());
        return new Vehicle( sellerName,
            sellerId,
            itemId,
            name,
            description,
            image, licensePlate, year, brand, color
        );
    }
}
