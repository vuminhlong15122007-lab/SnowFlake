package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Item extends Entity {
    private double initPrice, highestPrice, stepPrice;
    private Seller idSeller;
    private String idItem;
    private LocalDate startTime;
    private LocalDate endTime;
    private String description;
    private String imagePath; // đường dẫn ảnh

    public Item(String name, double initPrice, double highestPrice, double stepPrice, Seller idSeller, String idItem, LocalDate startTime, LocalDate endTime, String description, String imagePath) {
        super(name);
        this.initPrice = initPrice;
        this.highestPrice = highestPrice;
        this.stepPrice = stepPrice;
        this.idSeller = idSeller;
        this.idItem = idItem;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.imagePath = imagePath;
    }

    public double getInitPrice() {
        return initPrice;
    }

    public void setInitPrice(double initPrice) {
        this.initPrice = initPrice;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public double getStepPrice() {
        return stepPrice;
    }

    public void setStepPrice(double stepPrice) {
        this.stepPrice = stepPrice;
    }

    public Seller getIdSeller() {
        return idSeller;
    }

    public void setIdSeller(Seller idSeller) {
        this.idSeller = idSeller;
    }

    public String getIdItem() {
        return idItem;
    }

    public void setIdItem(String idItem) {
        this.idItem = idItem;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public LocalDate getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDate endTime) {
        this.endTime = endTime;
    }
    public void displayInfo(){
        System.out.println("========== Thông tin sản phẩm ==========");
        System.out.println("ID: " + this.idItem);
        System.out.println("Tên: " + this.getName());
        System.out.println("Mô tả: " + this.description);
        System.out.println("Giá khởi điểm: " + this.initPrice + " VND");
        System.out.println("Giá hiện tại: " + this.highestPrice + " VND");
        System.out.println("Bước giá: " + this.stepPrice + " VND");
        System.out.println("Thời gian bắt đầu: " + this.startTime);
        System.out.println("Thời gian kết thúc: " + this.endTime);

    }
}
