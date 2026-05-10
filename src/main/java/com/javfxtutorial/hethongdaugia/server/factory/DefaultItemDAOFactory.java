package com.javfxtutorial.hethongdaugia.server.factory;

import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;


public class DefaultItemDAOFactory extends ItemDAOFactory {
    @Override
    public DAOInterface createItemDAO() {
        return ItemDAO.getInstance(); // dùng ItemDAO thông thường
    }
}
