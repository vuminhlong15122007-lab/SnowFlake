package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;

import java.util.Map;

public abstract class ItemFactory {
    public abstract Item createItem(Map<String, String> data);
    public static ItemFactory getFactory(ItemCategory category){
        if(category == ItemCategory.VEHICLE) { return new VehicleFactory(); }
        else if (category == ItemCategory.ART) { return new ArtFactory(); }
        else if (category == ItemCategory.ELECTRONICS) { return new ElectronicsFactory(); }
        return new OtherItemFactory();
    }
}
