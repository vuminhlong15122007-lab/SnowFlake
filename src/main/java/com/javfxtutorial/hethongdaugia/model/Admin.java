package com.javfxtutorial.hethongdaugia.model;

public class Admin extends User {
    private double counter = 0;

    public Admin(String name, String passWord, String fullName, String email, double counter) {
        super(name, passWord, fullName, email);
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
