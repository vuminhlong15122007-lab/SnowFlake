package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.javfxtutorial.hethongdaugia.common.model.Item;

import java.io.IOException;
import java.time.LocalDateTime;


public class ManHinhHienThiSpController {
    @FXML private Label lbGiaSp;
    @FXML private Label lbTenngban;
    @FXML private Label lbTimer; // phan nay chx xu ly duoc
    @FXML private Button btnMenu;// phai lien ket  voi man chinh
    @FXML private Label lbAuctionName;
    @FXML private ImageView imgSanPham;


    private Item  TemMemory; // Bộ nhớ tạm thời để lưu sản phẩm đang xem
    private Parent TemListView;  // Luu lai man hinh Auction de con quay lai sau khi nhan btnMenu

    public void setProductData(Item p){ // nhan du lieu tu man Item..
        this.TemMemory = p;
        lbGiaSp.setText(String.valueOf(p.getCurrentPrice()));
        lbAuctionName.setText(p.getName());
        if (p.getImagePath() != null){
            String imagePath = "/com/javfxtutorial/hethongdaugia/assets/" + p.getImagePath();
            Image image = new Image(getClass().getResourceAsStream(imagePath));  // Tao tam anh tu duong dan
            imgSanPham.setImage(image); // dan tam anh vao khung
        }
//        lbTenngban.setText(p.getSellerId());  - man hinh Item khong co sellerid .. nghi cach luu vao cai j do de co the man hinh nay sd
        // chua xu ly duoc phan hien tgian
    }

    @FXML
    public void initialize(){


    }

    @FXML
    public void QuaylaiMenu(ActionEvent event){
        Platform.runLater(() -> {   // Nạp sẵn Màn hình 1 vào bộ nhớ ngay khi Màn 3 hiện lên
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/auction_list.fxml"));
                TemListView = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (TemListView != null){
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(TemListView);
        }else{
            // chua kip nap xong man
            try{
               Parent root = FXMLLoader.load(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
               Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
               stage.getScene().setRoot(root);
               
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    @FXML
    public void goToManHinhDauGiaTrucTiep(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/dau_gia_truc_tiep.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            ClientModel.getInstance().setCurrentAuction(new Auction(1, 1, 1000, 50, LocalDateTime.now(), LocalDateTime.now()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
