package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.User;

public class ClientModel {
    private static ClientModel instance;
    private ClientModel(){};

    public static ClientModel getInstance() {
        if (instance == null){
            return new ClientModel();
        }
        return instance;
    }


    private User currentUser;
    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }



    private Auction currentAuction;
    public Auction getCurrentAuction() {
        return currentAuction;
    }
    public void setCurrentAuction(Auction currentAuction) {
        this.currentAuction = currentAuction;
    }
}
