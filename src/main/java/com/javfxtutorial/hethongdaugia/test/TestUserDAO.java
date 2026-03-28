package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.model.Bidder;
import com.javfxtutorial.hethongdaugia.model.User;

public class TestUserDAO {
    static void main() {
        User u = new Bidder("Nguyên", "123456", "hnguyen@gmail.com");
        UserDAO.getInstance().create(u);
    }
}
