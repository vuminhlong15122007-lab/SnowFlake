package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;

public class AuctionManger {

    private static AuctionManger instance;
    private AuctionManger(){}
    public static AuctionManger getInstance(){
        if (instance == null){
            instance = new AuctionManger();
        }
        return instance;
    }

    Auction currentAuction;
    public void setCurrentAuction(Auction currentAuction) {
        this.currentAuction = currentAuction;
    }

    public boolean checkValidBid(double amount){
        double currentPrice = AuctionDAO.getInstance().selectById(currentAuction.getAuctionId()).getCurrentPrice(); //vào DAO check giá hiện tại
        if (amount >= currentPrice + currentAuction.getStepPrice()){
            return true;
        }
        System.out.println("Cần đặt giá cao hơn giá hiện tại + bước giá");
        return false;
    }

    public synchronized boolean placeBid(double amount){
        if (checkValidBid(amount)) {
            currentAuction = AuctionDAO.getInstance().selectById(currentAuction.getAuctionId());
            currentAuction.setCurrentPrice(amount);
            AuctionDAO.getInstance().update(currentAuction);
            return true;
        }
        return false;
    }
}
