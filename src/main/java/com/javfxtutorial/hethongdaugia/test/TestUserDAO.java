package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.database.JBDCUtil;
import com.javfxtutorial.hethongdaugia.model.User;

public class TestUserDAO {
    static void main() {
        System.out.println(UserDAO.getInstance().selectById(8));
        System.out.println(UserDAO.getInstance().selectByCondition("name = \"long\""));
    }
}
