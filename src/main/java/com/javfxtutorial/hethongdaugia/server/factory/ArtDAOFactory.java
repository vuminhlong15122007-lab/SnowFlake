package com.javfxtutorial.hethongdaugia.server.factory;

import com.javfxtutorial.hethongdaugia.server.dao.ArtDAO;
import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;

public class ArtDAOFactory extends ItemDAOFactory {
    @Override
    public DAOInterface createItemDAO() {
        return ArtDAO.getInstance();
    }
}