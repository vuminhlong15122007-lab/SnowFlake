package com.javfxtutorial.hethongdaugia.common.model;

import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Auction implements Serializable {
    private int auctionId;

    // Liên kết với sản phẩm đang được đấu giá
    // (Trong CSDL đây sẽ là Khóa ngoại - Foreign Key)
    private Item item;

    // Ai là người tổ chức phiên đấu giá này
    private int sellerId;

    // Các thông tin về giá
    private double initPrice;
    private double currentPrice;
    private double stepPrice;
    private double winningPrice; // Giá chốt cuối cùng (nếu có)

    // Thông tin về thời gian
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;

    // Trạng thái của phiên đấu giá
    private AuctionStatus status;

    // ID của người chiến thắng (sau khi phiên kết thúc)
    private int winnerId;

    public Auction(Item item, int sellerId, double initPrice, double stepPrice, LocalDateTime startingTime, LocalDateTime endingTime) {
        this.item = item;
        this.sellerId = sellerId;
        this.initPrice = initPrice;
        this.currentPrice = initPrice;
        this.stepPrice = stepPrice;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }


    public Auction(int auctionId, Item item, int sellerId, int winnerId, double initPrice, double currentPrice, double stepPrice, double winningPrice, LocalDateTime startingTime, LocalDateTime endingTime, AuctionStatus status) {
        this.auctionId = auctionId;
        this.item = item;
        this.sellerId = sellerId;
        this.initPrice = initPrice;
        this.currentPrice = currentPrice;
        this.stepPrice = stepPrice;
        this.winningPrice = winningPrice;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = status;
        this.winnerId = winnerId;
    }

    public Auction(Item item, int sellerId, double initPrice, double stepPrice, LocalDateTime startingTime, LocalDateTime endingTime, AuctionStatus status) {
        this.item = item;
        this.sellerId = sellerId;
        this.initPrice = initPrice;
        this.currentPrice = initPrice;
        this.stepPrice = stepPrice;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = status;
    }




    public int getAuctionId() {
        return auctionId;
    }
    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }
    public Item getItem() {return item;}
    public void setItem(Item item) {this.item = item;}
    public int getSellerId() {
        return sellerId;
    }
    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }
    public double getInitPrice() {
        return initPrice;
    }
    public void setInitPrice(double initPrice) {
        this.initPrice = initPrice;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
    public double getStepPrice() {
        return stepPrice;
    }
    public void setStepPrice(double stepPrice) {
        this.stepPrice = stepPrice;
    }
    public double getWinningPrice() {
        return winningPrice;
    }
    public void setWinningPrice(double winningPrice) {
        this.winningPrice = winningPrice;
    }
    public LocalDateTime getStartingTime() {
        return startingTime;
    }
    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }
    public LocalDateTime getEndingTime() {
        return endingTime;
    }
    public void setEndingTime(LocalDateTime endingTime) {
        this.endingTime = endingTime;
    }
    public AuctionStatus getStatus() {
        return status;
    }
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    public int getWinnerId() {
        return winnerId;
    }
    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "auctionId=" + auctionId +
                ", itemId=" + item.getItemId() +
                ", sellerId=" + sellerId +
                ", initPrice=" + initPrice +
                ", currentPrice=" + currentPrice +
                ", stepPrice=" + stepPrice +
                ", winningPrice=" + winningPrice +
                ", startingTime=" + startingTime +
                ", endingTime=" + endingTime +
                ", status=" + status +
                ", winnerId=" + winnerId +
                '}';
    }
}
