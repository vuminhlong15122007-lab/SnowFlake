package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.User;

public class ClientModel {
    private static ClientModel instance;
    private ClientModel(){};

    public static ClientModel getInstance() {
        if (instance == null){
            instance = new ClientModel();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private User currentUser;
}
