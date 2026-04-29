package com.javfxtutorial.hethongdaugia.common.model;

import java.io.Serializable;

public class AutoBidConfig implements Serializable {
    private int userId;
    private String userName;
    private int auctionId;
    private double maxPrice;
    private boolean isActive;

    public AutoBidConfig(int userId,String userName, int auctionId, double maxPrice, boolean isActive) {
        this.userId = userId;
        this.userName = userName;
        this.auctionId = auctionId;
        this.maxPrice = maxPrice;
        this.isActive = isActive;
    }

    public String getUserName() {return userName;}
    public void setUserName(String userName) {this.userName = userName;}

    public AutoBidConfig() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }
    public double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(double maxPrice) { this.maxPrice = maxPrice; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}