package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.database.JBDCUtil;
import com.javfxtutorial.hethongdaugia.model.User;

public class TestUserDAO {
    static void main() {
        User user = new User("haha", "haha", "haha", "haha");
        UserDAO.getInstance().insert(user);
        UserDAO.getInstance().selectAll();
        UserDAO.getInstance().delete(user);
        UserDAO.getInstance().selectAll();

    }
}
