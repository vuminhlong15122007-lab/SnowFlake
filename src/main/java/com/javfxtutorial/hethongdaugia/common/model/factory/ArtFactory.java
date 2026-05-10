package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Item;

public class ArtFactory extends ItemFactory{

    private int sellerId;
    private String name, description, image, sellerName;
    private String artist;
    private int yearCreated;
    private  String title;

    public ArtFactory(int sellerId, String name, String description, String image,
                      String sellerName, String artist, int yearCreated, String title) {
        this.sellerId = sellerId; this.name = name;
        this.description = description; this.image = image;
        this.sellerName = sellerName;
        this.artist = artist; this.yearCreated = yearCreated;
        this.title = title;
    }

    public ArtFactory() {}

    @Override
    public Item createItem() {
        return new Art();
    }
}
