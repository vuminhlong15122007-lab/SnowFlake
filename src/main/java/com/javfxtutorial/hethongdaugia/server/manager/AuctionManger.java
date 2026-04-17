package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;

public class AuctionManger {

    private static AuctionManger instance;
    private AuctionManger(){}
    public static AuctionManger getInstance(){
        if (instance == null){
            instance = new AuctionManger();
        }
        return instance;
    }

    public Auction getCurrentAuction(int auctionId){
        return AuctionDAO.getInstance().selectById(auctionId);
    }

    public synchronized boolean checkValidBid(Auction currentAuction, double amount){
        double currentPrice = currentAuction.getCurrentPrice();//vào DAO check giá hiện tại
        if (amount >= currentPrice + currentAuction.getStepPrice()){
            return true;
        }
        return false;
    }

    public synchronized boolean placeBid(Auction currentAuction, double amount){
        if (checkValidBid(currentAuction, amount)) {
            currentAuction.setCurrentPrice(amount);
            AuctionDAO.getInstance().update(currentAuction);
            return true;
        }
        return false;
    }

    public AuctionStatus refreshAuctionStatus(Auction auction){
        AuctionStatus previousStatus = auction.getStatus();
        if (LocalDateTime.now().isBefore(auction.getStartingTime())){
            auction.setStatus(AuctionStatus.NOT_START);
        }
        else if (LocalDateTime.now().isAfter(auction.getEndingTime())){
            auction.setStatus(AuctionStatus.CLOSED);
        }
        else {
            auction.setStatus(AuctionStatus.RUNNING);
        }
        if (previousStatus != auction.getStatus()){
        AuctionDAO.getInstance().update(auction);}
        return auction.getStatus();
    }

    //hien thi alert
    public void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
