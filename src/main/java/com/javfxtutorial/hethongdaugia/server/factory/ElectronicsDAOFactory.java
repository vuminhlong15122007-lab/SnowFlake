package com.javfxtutorial.hethongdaugia.server.factory;


import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.dao.ElectronicsDAO;

public class ElectronicsDAOFactory extends ItemDAOFactory {
    @Override
    public DAOInterface createItemDAO() {
        return ElectronicsDAO.getInstance();
    }
}
