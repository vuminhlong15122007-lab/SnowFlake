package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddItemCommand;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class QuanLySpSellerController {
    @FXML  TextField nameField;
    @FXML TextArea descriptionField;
    @FXML  TextField priceField;
    @FXML  TextField tfstepPrice;
    @FXML DatePicker startDatePicker;
    @FXML Spinner startHourSpinner;
    @FXML Spinner startMinuteSpinner;
    @FXML DatePicker endDatePicker;
    @FXML Spinner endHourSpinner;
    @FXML Spinner endMinuteSpinner;
    @FXML ImageView img;


    private Item product;
    // Khoi tao danh sach Observable
    private ObservableList<Item> observable = FXCollections.observableArrayList();

    public void goMenu(ActionEvent event){    // xu ly chuyen nut
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    String imagePath = "/com/javfxtutorial/hethongdaugia/assets/Logo.png"  ;

    @FXML public void upDate( ActionEvent event){       // btn Lưu
        try{
            String ten = nameField.getText();          // Thu thap du lieu ma nguoi dung da nhap
            String moTa = descriptionField.getText();
            double giaKhoiDiem = Double.valueOf(priceField.getText());
            double buocGia  = Double.valueOf(tfstepPrice.getText());
//            if (product.getImagePath() != null){
//            try{
//                // 1. Đường dẫn phải bắt đầu bằng dấu /
//                imagePath = "/com/javfxtutorial/hethongdaugia/assets/" + product.getImagePath();
//                Image image = new Image(getClass().getResourceAsStream(imagePath));  // Tao tam anh tu duong dan
//                img.setImage(image); // dan tam anh vao khung
//            }catch(Exception e){
//                e.printStackTrace();
//            }


            // Xu ly thoi gian
            LocalDate ngayBD = startDatePicker.getValue();
            int starhour = (int) startHourSpinner.getValue();
            int starminu = (int) startMinuteSpinner.getValue();

            // Xu ly thoi gian
            LocalDate ngayKT = endDatePicker.getValue();
            int endhour = (int) endHourSpinner.getValue();
            int endminu = (int) endMinuteSpinner.getValue();

            LocalDateTime tGianBD = LocalDateTime.of(ngayBD, LocalTime.of(starhour, starminu));
            LocalDateTime tGianKT = LocalDateTime.of(ngayKT, LocalTime.of(endhour, endminu));

            int sellerID = ClientModel.getInstance().getCurrentUser().getId();
            Item item = new Item(sellerID, ten, moTa, imagePath);
            Auction auction = new Auction(item.getItemId(), sellerID, giaKhoiDiem, buocGia,tGianBD, tGianKT );

            ServerConnection connection = new ServerConnection();
            Command cm = new AddItemCommand();
            cm.addData("Item", item);
            cm.addData("Auction", auction);

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Giá khởi điểm hoặc bước giá phải là số!");
        } catch (NullPointerException e) {
            System.out.println("Lỗi: Vui lòng nhập đúng định dạng!");
        }



    }







}
