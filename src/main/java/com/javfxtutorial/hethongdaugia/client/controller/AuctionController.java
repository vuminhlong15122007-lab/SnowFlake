package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllItemsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;


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

        // load du lieu
        loadData();

        // Xu ly o tim kiem - TexField
        // 1.Khoi tao  FilteredList - loc du lieu de hien thi
        FilteredList<Item>  filterData = new FilteredList<>(observable, p-> true );

        //2.Lang nghe o nhap du lieu
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filterData.setPredicate(product->
                {
                    if (newValue == null|| newValue.isEmpty()) return true; // o nhap vao du lieu trong => ds ban dau
                    String isLowerCase = newValue.toLowerCase(); // chuyen chu nguoi dung thanh chu thuong
                    String isLowerCase2 = product.getName().toLowerCase(); // chuyen ten sp trong item thanh chu thuong
                    return isLowerCase2.contains(isLowerCase); // sp trong system chi can chua ten sp ma ng nhap thi se in ra ten sp trong system do

                }));


        featuredProductList.setCellFactory(lv -> new ProductCell());  //lệnh cài đặt cách hiển thị cho ListView.
        featuredProductList.setItems(filterData);
    }


    public void loadData(){
       //observable.add(new Item()
//        ServerConnection connection = new ServerConnection();
//        Command cmd = new GetAllItemsCommand();
//        Response rp = connection.sendCommand(cmd);
//        ObservableList<Item> itemList = (ObservableList<Item>) rp.getPayLoad();
//        if (itemList != null){
//            observable.addAll(itemList);
//        }
        observable.add(new Item(1, 1, "test", "teset", "aaaa", 2000, 2000));
        ClientModel.getInstance().setCurrentItem(new Item(1, 1, "test", "teset", "aaaa", 2000, 2000));
        ClientModel.getInstance().setCurrentAuction(new Auction(1, 1, 200, 2000, LocalDateTime.now(), LocalDateTime.now()));
    }


    public void logOut1(ActionEvent event){
        try {

            // Nap man hinh giao dien man_hinh_sp
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/login.fxml"));
            Parent root = loader.load();  // tim file FXML doc ban ve va tao giao dien xac ( chua co bo nao)

            // Lech chuyen man
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);  //Chuyen man = setRoot

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void manageProducts(ActionEvent event){
        try {

            // Nap man hinh giao dien man_hinh_sp
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/quan_ly_san_pham_seller.fxml"));
            Parent root = loader.load();  // tim file FXML doc ban ve va tao giao dien xac ( chua co bo nao)

            // Lech chuyen man
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);  //Chuyen man = setRoot

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void btnHome(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void goToProfile(ActionEvent event){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/man_hinh_hien_thong_tin_User.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
