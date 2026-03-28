package com.javfxtutorial.hethongdaugia.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity {
    private int counterId = 0;
    private Item item;
    private BidTransaction WinningBid;
    private boolean isActive;
    private List<BidTransaction> bidHistory;

    public Auction(Item item, String name) {
        super(name);
        counterId ++;
        this.item = item;
        this.bidHistory = new ArrayList<BidTransaction>();



    }

    public boolean checkActive(){
        LocalDate now = LocalDate.now();
        if (now.isAfter(item.getEndTime())){
            return false;
        }
        return true;
    }

    public boolean placeBid(Bidder bidder, double amount){
        if(!isActive){
            System.out.println("Phiên đấu giá đã kết thúc");
            return false;
        }

        if(amount < item.getStepPrice() + item.getHighestPrice()){
            System.out.println(String.format("Cần đặt giá cao hơn: %d", item.getHighestPrice() + item.getStepPrice()));
            return false;
        }

        BidTransaction new_transaction = new BidTransaction(bidder, amount, LocalDate.now());
        bidHistory.add(new_transaction);
        item.setHighestPrice(amount) ;
        this.WinningBid = new_transaction;
        return true;
    }

    public BidTransaction getWinningBid() {
        return WinningBid;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    @Override
    public String Generate_Id(){
        int counter  = this.getCountAuction() + 1;
        this.setCountAuction(counter);
        return "AU" + String.format("%03d", counter);
    }
}

