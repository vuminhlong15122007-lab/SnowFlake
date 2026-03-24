package com.javfxtutorial.hethongdaugia.model;

public class Admin extends User {
    private double counter = 0;

    public Admin(String id, String name, String passWord, String fullName, String email, double counter) {
        super(id, name, passWord, fullName, email);
        this.counter = counter;
    }

    public void banUser(){

    }

    public void unBanUser(){

    }

    public void cancelAution(){

    }

    @Override
    public String Generate_Id(){
        counter ++;
        return "AD " + counter + String.format("%03d", counter);
    }
}
