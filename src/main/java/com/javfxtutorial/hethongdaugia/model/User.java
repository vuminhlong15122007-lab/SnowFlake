package com.javfxtutorial.hethongdaugia.model;

import java.time.LocalDate;

public abstract class User extends Entity {
    private String passWord;
    private String email;
    private String fullName;

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
}
