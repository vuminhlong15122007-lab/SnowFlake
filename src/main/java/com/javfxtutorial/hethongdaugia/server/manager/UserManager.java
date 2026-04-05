package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.common.model.User;

public class UserManager {
    private static UserManager instance;
    private UserManager(){}
    public static UserManager getInstance() {
        if (instance == null){
            instance = new UserManager();
        }
        return instance;
    }

    public User authenticate(String username, String password){
        User user = UserDAO.getInstance().selectByUsername(username);
        if (user != null && user.getPassWord().equals(password)){
            return user;
        }
        return null;
    }


}
