package com.javfxtutorial.hethongdaugia.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidTransaction implements Serializable {
    private int bidId;
    private int bidderId;
    private String bidderName;
    private int auctionId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private LocalDateTime newEndingTime;
    private String bidderEmail = "";
    private String bidderSdt = "";



    public BidTransaction(String bidderName, int auctionId, BigDecimal amount, LocalDateTime timestamp) {
        this.bidderName = bidderName;
        this.auctionId = auctionId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public BidTransaction() {}

    public LocalDateTime getNewEndingTime() {
        return newEndingTime;
    }
    public void setNewEndingTime(LocalDateTime newEndingTime) {
        this.newEndingTime = newEndingTime;
    }
    public int getBidId() {
        return bidId;
    }
    public void setBidId(int bidId) {
        this.bidId = bidId;
    }
    public String getBidderName() {
        return bidderName;
    }
    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
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
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public String getBidderEmail() { return bidderEmail; }
    public void setBidderEmail(String v) { this.bidderEmail = v; }
    public String getBidderSdt() { return bidderSdt; }
    public void setBidderSdt(String v) { this.bidderSdt = v; }
}
