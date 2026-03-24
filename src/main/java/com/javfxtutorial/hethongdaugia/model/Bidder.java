package com.javfxtutorial.hethongdaugia.model;

public class Bidder extends User {
    private double Money;
    private int counterID = 0;

    public double getMoney() {
        return Money;
    }

    public void setMoney(double money) {
        Money = money;
    }

    public int getCounterID() {
        return counterID;
    }

    public void setCounterID(int counterID) {
        this.counterID = counterID;
    }

    public String Generate_Id(){
        this.setCounterID(this.getCounterID() + 1);
        return "BD" + String.format("%03d", this.getCounterID());
    }
    public Bidder(String id , String name ; String passWord ; String email ; String fullName ; double money){
        supper()
    }
}
