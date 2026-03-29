package com.javfxtutorial.hethongdaugia.model;

import java.time.LocalDate;

public class User extends Entity {
    private String passWord;
    private String email;
    private String fullName;
    private int countSeller = 0 ;
    private int countBidder = 0;
    private int countAdmin = 0 ;

    public int getCountAdmin() {
        return countAdmin;
    }

    public void setCountAdmin(int countAdmin) {
        this.countAdmin = countAdmin;
    }

    public int getCountSeller() {
        return countSeller;
    }

    public void setCountSeller(int countSeller) {
        this.countSeller = countSeller;
    }

    public int getCountBidder() {
        return countBidder;
    }

    public void setCountBidder(int countBidder) {
        this.countBidder = countBidder;
    }

    public User(String name, String passWord, String email) {
        super(name);
        this.passWord = passWord;
        this.email = email;
    }

    public User(String name, String passWord, String fullName, String email) {
        super(name);
        this.passWord = passWord;
        this.fullName = fullName;
        this.email = email;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    @Override
    public String Generate_Id() {
        return "";
    }
}
