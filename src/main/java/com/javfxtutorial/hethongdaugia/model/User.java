package com.javfxtutorial.hethongdaugia.model;

import java.time.LocalDate;

public abstract class User extends Entity {
    private String passWord;
    private String email;
    private String fullName;

    public User(String id, String name, String passWord, String fullName, String email) {
        super(id, name);
        this.passWord = passWord;
        this.fullName = fullName;
        this.email = email;
    }



}
