package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;

import com.javfxtutorial.hethongdaugia.model.enums.ItemStatus;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AuctionItem{
    private double initPrice, currentPrice, endingPrice, stepPrice;
    private int sellerId;
    private String itemId;
    private String name;
    private LocalDate startingTime;
    private LocalDate endingTime;
    private String description;
    private String imagePath; // đường dẫn ảnh
    private ItemStatus itemStatus;


    public ItemStatus getStatus() {
        return itemStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(ItemStatus status) {
        this.itemStatus = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalDate endingTime) {
        this.endingTime = endingTime;
    }

    public LocalDate getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalDate startingTime) {
        this.startingTime = startingTime;
    }

    public String getItemId() {
        return itemId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public double getStepPrice() {
        return stepPrice;
    }

    public void setStepPrice(double stepPrice) {
        this.stepPrice = stepPrice;
    }

    public double getEndingPrice() {
        return endingPrice;
    }

    public void setEndingPrice(double endingPrice) {
        this.endingPrice = endingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getInitPrice() {
        return initPrice;
    }

    public void setInitPrice(double initPrice) {
        this.initPrice = initPrice;
    }

    public void displayInfo(){
        System.out.println("========== Thông tin sản phẩm ==========");
        System.out.println("ID: " + this.itemId);
        System.out.println("Tên: " + this.getName());
        System.out.println("Mô tả: " + this.description);
        System.out.println("Giá khởi điểm: " + this.initPrice + " VND");
        System.out.println("Giá hiện tại: " + this.currentPrice + " VND");
        System.out.println("Bước giá: " + this.stepPrice + " VND");
        System.out.println("Thời gian bắt đầu: " + this.startingTime);
        System.out.println("Thời gian kết thúc: " + this.endingPrice);

    }
}
