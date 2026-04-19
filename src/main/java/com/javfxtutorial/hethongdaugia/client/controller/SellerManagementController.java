package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.*;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class SellerManagementController {
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
    @FXML ListView<Auction> productList;


    private Item product;
    // Khoi tao danh sach Observable
    private ObservableList<Auction> observable = FXCollections.observableArrayList();

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

    public Auction getInfo(){
        String name = nameField.getText();          // Thu thap du lieu ma nguoi dung da nhap
        String description = descriptionField.getText();
        double initPrice = Double.parseDouble(priceField.getText());
        double stepPrice  = Double.parseDouble(tfstepPrice.getText());


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
        //Lấy xong dữ liệu người dùng nhập vào
        int sellerId = ClientModel.getInstance().getCurrentUser().getId();
        String sellerName = ClientModel.getInstance().getCurrentUser().getName();

        Item item = new Item(sellerId, name, description, imagePath, sellerName);
        Auction auction = new Auction(item, sellerId, initPrice, stepPrice, tGianBD, tGianKT, AuctionStatus.NOT_START);

        return auction;

    }

    String imagePath = "/com/javfxtutorial/hethongdaugia/assets/Logo.png"  ;

    @FXML public void luuSp( ActionEvent event) throws IOException {       // btn Lưu
        try{
            Auction auction = getInfo();

            //thêm auction vào DAO và hiện ra list bên trái
            ServerConnection connection = new ServerConnection();
            AddAuctionCommand cm = new AddAuctionCommand();
            cm.addData("Auction", auction);
            new Thread(() -> {     // Tao 1 luong phụ để để gửi dữ liệu về server và chuyển dữ liệu từ server về
                try {
                    connection.sendCommand(cm);
                    Response response = connection.receiveResponse();// method gui du lieu ve sẻrver
                    Platform.runLater(() -> {   // gọi Thread Main cập nhận giao diện
                        if (response.isSuccess()) {
                            Auction savedAuction = (Auction) response.getPayLoad();
                            observable.add(savedAuction);
                            showAlert("Thành công", "Thêm sản phẩm thành công!");
                        } else{
                            System.out.println(response.getMessage());
                        }
//
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Lỗi kết nối server!");
                    showAlert("Lỗi!!", "Xem lại nội dung mình nhập");
//
                }
            }).start();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Lỗi không nhập đúng format");
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("Lỗi đinh dạng");
        }
    }


    public void initialize() throws IOException, ClassNotFoundException {
        //  Cấu hình Spinner cho giờ phút
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        productList.setItems(observable);   //dùng để gắn một nguồn dữ liệu obs vào ListView (method có sẵn của class ListView<T>)
        productList.setCellFactory((ListView<Auction> listView) -> new ProductCell2()); // trả về một instance của ProductCell2 –class tự load giao diện cell tùy chỉnh

        loadMyProducts();  // Tải danh sách ban đùa của server ứng với IDserver ng dùng đăng nhập và đổ vào obs

        // Lắng nghe sự kiện khi người dùng chọn sp
        productList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hienThiChiTietSanPham(newVal);      // LUÔN hiển thị thông tin
            }
        });
    }

    private void loadMyProducts() throws IOException, ClassNotFoundException {
        int sellerId = ClientModel.getInstance().getCurrentUser().getId();
// Lấy ID của user đang đăng nhập => gửi lên server => server lọc sp của sellerId lưu vào cmd

        ServerConnection connection = new ServerConnection(); // mở đường dây liên lạc với server
        Command cmd = new GetAuctionsBySellerIdCommand();
        cmd.addData("sellerId", sellerId);

        new Thread(() -> { //Tạo 1 luồng giao diện mới và giao công vc cho luồng
            try {
                connection.sendCommand(cmd);
                Response resp = connection.receiveResponse();   //cmd biến thành dãy bit gửi qua mạng => server xưr lý => gửi về một Response chứa danh sách Item.

                Platform.runLater(() -> {          //luồng phụ gọi để luồng chính xử lý
                    if (resp.isSuccess()) {  // kiểm tra xem có nhận được danh sách cân ko
                        ArrayList<Auction> auctions = (ArrayList<Auction>) resp.getPayLoad();  // lấy đồ ra
                        observable.setAll(auctions); // sắp xếp lên listView
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

            }
        }).start();
    }

    @FXML
    public void deleteAuction(ActionEvent event) throws IOException {  // xử lý nút xóA
        Auction selected = productList.getSelectionModel().getSelectedItem();  // Lấy ttin sản phẩm đang được chọn ( của listView)
        if (selected == null) {
            System.out.println(" Vui lòng nhấn chọn sản phẩm");
        }

        GetAuctionStatusCommand cmd = new GetAuctionStatusCommand(selected);
        ServerConnection connection = new ServerConnection();
        new Thread(() -> {
            try {
                connection.sendCommand(cmd);
                Response resp = connection.receiveResponse(); // gửi yêu cầu lên server và server xử lý
                AuctionStatus nowStatus = (AuctionStatus) resp.getPayLoad();
                if(nowStatus == AuctionStatus.NOT_START){
                    DeleteAuctionCommand cm = new DeleteAuctionCommand(selected);  // tạo yêu cầu xóa sp cso ID ..
                    connection.sendCommand(cm);
                    Response rp = connection.receiveResponse(); // gửi yêu cầu lên server và server xử lý
                    Platform.runLater(() -> {
                        if (rp.isSuccess()) {  //check xem yc đã đc thực hiện chx
                            observable.remove(selected);// xóa sp khỏi ds
                            showAlert("Thành công", "Xóa sản phẩm thành công!" );
                        }else{
                            System.out.println(resp.getMessage());
                        }
                    });
                }else{
                    showAlert("Không xóa sửa sản phẩm", "Phiên đấu giá đang diễn ra hoặc đã kết thúc");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi!!!", "Xem lại thao tác!");
            }
        }).start();

    }

    @FXML
    public void updateImg(ActionEvent event){
        // chưa xử lý
    }


    public void hienThiChiTietSanPham(Auction auction){
        nameField.setText(auction.getItem().getName());          // Thu thap du lieu ma nguoi dung da nhap
        descriptionField.setText(auction.getItem().getDescription());
        priceField.setText(String.format("%,.0f", auction.getCurrentPrice()));
        tfstepPrice.setText(String.valueOf(auction.getStepPrice()));
        LocalDateTime start = auction.getStartingTime();
        if (start != null) {
            startDatePicker.setValue(start.toLocalDate());
            startHourSpinner.getValueFactory().setValue(start.getHour());
            startMinuteSpinner.getValueFactory().setValue(start.getMinute());
        }

        // Hiển thị thời gian kết thúc
        LocalDateTime end = auction.getEndingTime();
        if (end != null) {
            endDatePicker.setValue(end.toLocalDate());
            endHourSpinner.getValueFactory().setValue(end.getHour());
            endMinuteSpinner.getValueFactory().setValue(end.getMinute());
        }

        // ẢNH - CHƯA XỬ LÝ LẤY RA
    }


    @FXML
    public void suaSp  (ActionEvent event) throws IOException, IllegalStateException{  // Xử lý nuts Sửa
        Auction selected = productList.getSelectionModel().getSelectedItem();  // Lấy ttin sản phẩm đang được chọn ( của listView)
        if (selected == null) {
            System.out.println(" Vui lòng nhấn chọn sản phẩm");
        }
        GetAuctionStatusCommand statusCmd = new GetAuctionStatusCommand(selected);
        ServerConnection connection = new ServerConnection();

        new Thread(() -> { //Tạo 1 luồng giao diện mới và giao công vc cho luồng
            try{
                connection.sendCommand(statusCmd);
                Response resp = connection.receiveResponse();

                if (!resp.isSuccess()) {
                    Platform.runLater(() -> showAlert("Lỗi", resp.getMessage()));
                }

                // 1. Lấy dữ liệu từ form NGAY BÂY GIỜ (trên UI Thread)
                Auction auction = getInfo();
                auction.setAuctionId(selected.getAuctionId());
                auction.getItem().setItemId(selected.getItem().getItemId());

                AuctionStatus nowStatus = (AuctionStatus) resp.getPayLoad();
                if(nowStatus == AuctionStatus.NOT_START){
                    UpdateAuctionCommand cmd = new UpdateAuctionCommand(auction);
                    connection.sendCommand(cmd);
                    Response rp = connection.receiveResponse();
                    Platform.runLater(() -> {
                        if (rp.isSuccess()) {
                            int index = observable.indexOf(selected);
                            observable.set(index, auction);
                            productList.refresh();
                            showAlert("Thành công", "Sửa sản phẩm thành công");
                        }else{
                            System.out.println(resp.getMessage());
                        }
                    });
                }else{
                    Platform.runLater(() ->
                            showAlert("Không thể xóa", "Phiên đấu giá đang diễn ra hoặc đã kết thúc")
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Lỗi!!!", "Xem lại thao tác!"));

            } finally {
                try {
                    connection.close(); // <-- THÊM DÒNG NÀY
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    //hien thi alert
    public void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
