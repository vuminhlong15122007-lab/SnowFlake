package com.javfxtutorial.hethongdaugia.server.factory;


import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.server.dao.ArtDAO;
import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.dao.ElectronicsDAO;
import com.javfxtutorial.hethongdaugia.server.dao.VehicleDAO;


public abstract class ItemDAOFactory {
    public abstract DAOInterface createItemDAO();

    public static ItemDAOFactory getFactory(ItemCategory category){
        if(category == ItemCategory.Vehicle){
            return new VehicleDAOFactory();
        } else if (category == ItemCategory.Art) {
            return new ArtDAOFactory();
        } else if (category == ItemCategory.Electronics) {
            return new ElectronicsDAOFactory();
        }
        return null;
    }

}
