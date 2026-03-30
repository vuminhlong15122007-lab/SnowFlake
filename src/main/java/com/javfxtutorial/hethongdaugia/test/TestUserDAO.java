package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.database.JBDCUtil;
import com.javfxtutorial.hethongdaugia.model.User;

public class TestUserDAO {
    static void main() {
        User u = new User("Nguyên", "123456", "hnguyen@gmail.com", "0997832005");
        UserDAO.getInstance().insert(u);
        u.setName("Long");
        UserDAO.getInstance().update(u);

    }
}
