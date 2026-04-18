package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ResourceBundle;


public class LiveAuctionController implements Initializable {
    Auction currentAuction;
    ServerConnection connection = new ServerConnection();
    @FXML
    TextField priceInput_tf;
    @FXML
    Label highestPayer_tf;
    @FXML
    Label currentPrice_tf;
    @FXML
    Label stepPrice_tf;
    @FXML
    Label itemNameLb;

    @FXML
    public void goMenu(ActionEvent event){
        try{
            connection.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void placeBid (ActionEvent event) throws IOException, ClassNotFoundException {
        System.out.println("Bạn vừa ấn placeBid");
        BidTransaction bid = new BidTransaction();
        bid.setBidderId(ClientModel.getInstance().getCurrentUser().getId());
        bid.setAmount(Double.parseDouble(priceInput_tf.getText()));
        bid.setAuctionId(ClientModel.getInstance().getCurrentAuction().getAuctionId());
        bid.setTimestamp(LocalDate.now());



        Command cmd = new PlaceBidCommand();
        cmd.addData("bid", bid);
        cmd.addData("currentAuction", currentAuction);
        connection.sendCommand(cmd);
        System.out.println("Đã send command");
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentAuction = ClientModel.getInstance().getCurrentAuction();
        currentPrice_tf.setText(String.valueOf(currentAuction.getCurrentPrice()));
        stepPrice_tf.setText(String.valueOf(currentAuction.getStepPrice()));
        highestPayer_tf.setText(String.valueOf(currentAuction.getWinnerId()));
        itemNameLb.setText(ClientModel.getInstance().getCurrentItem().getName());
        System.out.println("Đã load xong giao diện");

        connectToServer();
    }

    private void connectToServer(){ //Khởi tạo một luồng riêng để luôn nhận phản hồi từ server mà không gây lag, đơ)
        Thread thread = new Thread(() -> {
            while (true){
                try {
                    Response rp = connection.receiveResponse();
                    if (rp.getCommand() instanceof PlaceBidCommand){ //liên tục cập nhật giá cao nhất
                        //lấy giá mới từ server trả về (mỗi khi có client đặt giá, server sẽ thông báo cho tất cả client đang hoạt động)
                        BidTransaction bid = (BidTransaction) rp.getPayLoad();

                        if (ClientModel.getInstance().getCurrentUser().getId() == bid.getBidderId()){
                            Platform.runLater(() -> {
                            showAlert("Trạng thái đặt bid", rp.getMessage());});
                        }

                        if (rp.isSuccess()) {
                            if (bid.getAuctionId() == currentAuction.getAuctionId()) { //kiểm tra xem có trùng id auction không
                                double newPrice = bid.getAmount();
                                int bidderId = bid.getBidderId();

                                currentAuction.setCurrentPrice(newPrice);
                                currentAuction.setWinnerId(bidderId);
                                currentAuction.setWinningPrice(newPrice);


                                //trong javafx, chỉ có luồng chính mới có thể sửa UI, gọi platform runlater để gọi luồng chính cập nhật giao diện
                                Platform.runLater(() -> {
                                    currentPrice_tf.setText(Double.toString(newPrice));
                                    highestPayer_tf.setText(String.valueOf(bidderId));
                                });
                            }
                        }

                    }

                } catch (IOException | ClassNotFoundException e) {
                    try {
                        connection.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
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
