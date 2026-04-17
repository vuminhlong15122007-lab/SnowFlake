package com.javfxtutorial.hethongdaugia.common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class BidTransaction implements Serializable {
    private int bidId;
    private int bidderId;
    private int auctionId;
    private double amount;
    private LocalDate timestamp;

    public BidTransaction(int bidderId, int auctionId, double amount, LocalDate timestamp) {
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public BidTransaction() {
    }

    public int getBidId() {
        return bidId;
    }

    public void setBidId(int bidId) {
        this.bidId = bidId;
    }

    public int getBidderId() {
        return bidderId;
    }

    public void setBidderId(int bidderId) {
        this.bidderId = bidderId;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }
}

