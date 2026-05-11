package com.javfxtutorial.hethongdaugia.server.factory;

import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.dao.VehicleDAO;

public class VehicleDAOFactory extends ItemDAOFactory {

    @Override
    public DAOInterface createItemDAO() {
        return VehicleDAO.getInstance();
    }
}
