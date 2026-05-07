package com.javfxtutorial.hethongdaugia.common.model;

public class Art extends Item {
    private String artist;
    private int yearCreated;
    private String title;

    public Art(int sellerId, String name, String description, String image, String sellerName, String artist, int yearCreated, String title) {
        super(sellerId, name, description, image, sellerName);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.title = title;
    }

    public Art() {super();}

    public String getArtist() {return artist;}
    public void setArtist(String artist) {this.artist = artist;}
    public int getYearCreated() {return yearCreated;}
    public void setYearCreated(int yearCreated) {this.yearCreated = yearCreated;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
}
