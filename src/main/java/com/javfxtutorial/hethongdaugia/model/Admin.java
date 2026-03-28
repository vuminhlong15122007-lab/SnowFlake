package com.javfxtutorial.hethongdaugia.model;

public class Admin extends User {
    private int counter = 0;

    public Admin(String name, String passWord, String fullName, String email, int counter) {
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
        counter  = this.getCountBidder() + 1;
        this.setCountBidder(counter);
        return "AD " + counter + String.format("%03d", counter);
    }
}
