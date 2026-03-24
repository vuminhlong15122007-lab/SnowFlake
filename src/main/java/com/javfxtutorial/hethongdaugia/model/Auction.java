package com.javfxtutorial.hethongdaugia.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Auction {
    private Item item;
    private BidTransaction WinningBid;
    private boolean isActive;
    private List<BidTransaction> bidHistory;

    public Auction(Item item) {
        this.item = item;
        this.bidHistory = new ArrayList<BidTransaction>();
    }

    public boolean checkActive(){
        LocalDate now = LocalDate.now();
        if (now.isAfter(item.endTime)){
            return false;
        }
        return true;
    }

    public boolean placeBid(Bidder bidder, double amount){
        if(!isActive){
            System.out.println("Phiên đấu giá đã kết thúc");
            return false;
        }

        if(amount < item.stepPrice + item.highestPrice){
            System.out.println(String.format("Cần đặt giá cao hơn: %d", highestPrice + item.stepPrice));
            return false;
        }

        BidTransaction new_transaction = new BidTransaction(bidder, amount, LocalDate.now());
        bidHistory.add(new_transaction);
        item.highestPrice = amount;
        this.WinningBid = new_transaction;
        return true;
    }

    public BidTransaction getWinningBid() {
        return WinningBid;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }
}
