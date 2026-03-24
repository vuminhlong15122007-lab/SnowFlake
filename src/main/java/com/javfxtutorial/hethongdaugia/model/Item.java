package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Item {
    private double initPrice, highestPrice, stepPrice;
    private Seller idSeller;
    private String idItem;
    private LocalDate startTime;
    private LocalDate endTime;
    public String description;
    private String imagePath; // đường dẫn ảnh



}
