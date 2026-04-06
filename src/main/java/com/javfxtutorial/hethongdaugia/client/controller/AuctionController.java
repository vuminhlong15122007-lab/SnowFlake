package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.javfxtutorial.hethongdaugia.common.model.Item;


public class AuctionController {

    @FXML ListView<Item>  featuredProductList;
    @FXML TextField searchField;
    @FXML Button btnHome;      // 4 cai button chx co hanh dong vs lk man khac
    @FXML Button btnLiveAuction;
    @FXML Button btnmanageProducts;
    @FXML Button btnprofile;


    // Khoi tao danh sach Observable
    private ObservableList<Item> observable = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        // Ep listView gian rong ra het co
        VBox.setVgrow(featuredProductList, Priority.ALWAYS); // listView tu gian rong het co theo chieu doc
        featuredProductList.setMaxWidth(Double.MAX_VALUE ); // gian rong het co theo chieu rong

        // Xu ly o tim kiem - TexField
        // 1.Khoi tao  FilteredList - loc du lieu de hien thi
        FilteredList<Item>  filterData = new FilteredList<>(observable, p-> true );

        // load du lieu
        loadData();

        //2.Lang nghe o nhap du lieu
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filterData.setPredicate(product->
                {
                    if (newValue == null|| newValue.isEmpty()) return true; // o nhap vao du lieu trong => ds ban dau
                    String isLowerCase = newValue.toLowerCase(); // chuyen chu nguoi dung thanh chu thuong
                    String isLowerCase2 = product.getName().toLowerCase(); // chuyen ten sp trong item thanh chu thuong
                    return isLowerCase2.contains(isLowerCase); // sp trong system chi can chua ten sp ma ng nhap thi se in ra ten sp trong system do

                }));

        featuredProductList.setItems(filterData); // ListView hien ti nhung gi ma filteredList muon
    }


    public void loadData(){
       //observable.add(new Item() {    CHUA BIET NEN LAP GI

        observable.add(new Item(32635, "shaj", "shagh", "gsja", "hsaj") {

        });

    }



}
