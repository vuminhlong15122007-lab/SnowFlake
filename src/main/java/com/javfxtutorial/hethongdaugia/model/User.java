package com.javfxtutorial.hethongdaugia.model;

import com.javfxtutorial.hethongdaugia.model.enums.AccountType;

import java.time.LocalDate;

public class User{
    private String passWord;
    private String email;
    private String name;
    private String sdt;
    private int id;
    private AccountType accountType;

    public User(String name, String passWord, String email, String sdt) {
        this.name = name;
        this.passWord = passWord;
        this.email = email;
        this.sdt = sdt;
    }

    public User(int id, String name, String passWord, String email, String sdt) {
        this.id = id;
        this.name = name;
        this.passWord = passWord;
        this.email = email;
        this.sdt = sdt;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                ", id=" + id + '\'' +
                ", name='" + name + '\'' +
                ", passWord='" + passWord + '\'' +
                ", email='" + email + '\'' +
                ", sdt='" + sdt + '\'' +
                ", accountType=" + accountType +
                '}';
    }

    public AccountType getAccountType() {
        return accountType;
    }
}


