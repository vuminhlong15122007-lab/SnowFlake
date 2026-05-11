package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.server.factory.ArtDAOFactory;
import com.javfxtutorial.hethongdaugia.server.factory.DefaultItemDAOFactory;
import com.javfxtutorial.hethongdaugia.server.factory.ElectronicsDAOFactory;
import com.javfxtutorial.hethongdaugia.server.factory.VehicleDAOFactory;

public abstract class ItemFactory {
    public abstract Item createItem();
    public static ItemFactory getFactory(ItemCategory category){
        if(category == ItemCategory.Vehicle) { return new VehicleFactory(); }
        else if (category == ItemCategory.Art) { return new ArtFactory(); }
        else if (category == ItemCategory.Electronics) { return new ElectronicsFactory(); }
        return new DefaultItemFactory();

    }
}
