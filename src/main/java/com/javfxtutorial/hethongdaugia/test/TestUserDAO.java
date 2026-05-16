package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;

public class TestUserDAO {
    static void main(String[] args) throws DataException {
        User user = new User("nguyênnnnn", "con", "danh", "con", AccountType.USER);
        UserDAO.getInstance().insert(user);

    }
}
