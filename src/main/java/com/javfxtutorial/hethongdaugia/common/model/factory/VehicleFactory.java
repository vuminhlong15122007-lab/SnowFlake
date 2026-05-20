package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import java.util.Map;

public class VehicleFactory extends ItemFactory {
    @Override
    public Item createItem(Map<String, String> data) {
        Vehicle v = new Vehicle();
        v.setSellerId(Integer.parseInt(data.get("sellerId")));
        v.setSellerName(data.get("sellerName"));
        v.setName(data.get("name"));
        v.setDescription(data.get("description"));
        v.setImage(data.get("image"));
        v.setCategory(ItemCategory.VEHICLE);
        v.setBrand(data.get("brand"));
        v.setLicensePlate(data.get("licensePlate"));
        v.setYear(Integer.parseInt(data.get("year")));
        v.setColor(data.get("color"));
        return v;
    }
}
