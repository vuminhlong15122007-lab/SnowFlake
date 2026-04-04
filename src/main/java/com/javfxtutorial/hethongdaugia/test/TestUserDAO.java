package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;

public class TestUserDAO {
    static void main() {
        User user = new User("danh", "con", "danh", "con", AccountType.USER);
        UserDAO.getInstance().insert(user);
        System.out.println(UserDAO.getInstance().selectAll());
        System.out.println(UserDAO.getInstance().selectById(user.getId()));
        System.out.println(UserDAO.getInstance().selectByCondition("name = \"danh\""));
        user.setName("lan");
        UserDAO.getInstance().update(user);
        UserDAO.getInstance().delete(user);
    }
}
