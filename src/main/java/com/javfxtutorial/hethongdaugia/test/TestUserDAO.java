package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.model.User;

public class TestUserDAO {
    public static void main() {
        User u = new User("Nguyên", "123456", "hnguyen@gmail.com","0343336718");
        UserDAO.getInstance().insert(u);
    }
}
