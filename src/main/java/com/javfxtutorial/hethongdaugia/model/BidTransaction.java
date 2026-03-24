package com.javfxtutorial.hethongdaugia.model;

import java.time.LocalDate;

public class BidTransaction extends Entity{
    private Bidder bidder;
    private double bidAmount;
    private LocalDate timestamp;

    public BidTransaction(Bidder bidder, double bidAmount, LocalDate timestamp) {
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }
}
