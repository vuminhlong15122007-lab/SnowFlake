package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class ManHinhDauGiaTrucTiep implements Initializable {
    Auction currentAuction;
    ServerConnection connection;
    @FXML
    TextField priceInput_tf;
    @FXML
    Label highestPayer_tf;
    @FXML
    Label currentPrice_tf;
    @FXML
    Label stepPrice_tf;


    public void goMenu(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void placeBid (ActionEvent event){
        double amount = Double.parseDouble(priceInput_tf.getText());
        Command cmd = new PlaceBidCommand();
        cmd.addData("amount", amount);
        cmd.addData("bidderName", ClientModel.getInstance().getCurrentUser().getName());
        cmd.addData("currentAuction", ClientModel.getInstance().getCurrentAuction());
        Response rp = connection.sendCommand(cmd);
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentAuction = ClientModel.getInstance().getCurrentAuction();
        System.out.println("Đã load xong giao diện");
        connectToServer();
    }

    private void connectToServer(){ //Khởi tạo một luồng riêng để luôn nhận phản hồi từ server mà không gây lag, đơ)
        new Thread(() -> {
            connection = new ServerConnection();
            while (true){
                try {
                    Response response = (Response) connection.getIn().readObject();
                    if (response.getCommand() instanceof PlaceBidCommand){ //liên tục cập nhật giá cao nhất
                        //lấy giá mới từ server trả về (mỗi khi có client đặt giá, server sẽ thông báo cho tất cả client đang hoạt động)
                        HashMap<String, Object> payload = (HashMap<String, Object>) response.getPayLoad();
                        Auction commandAuction = (Auction) payload.get("currentAuction");
                        if (commandAuction.getAuctionId() == currentAuction.getAuctionId()) { //kiểm tra xem có trùng id auction không
                            double newPrice = (double) payload.get("amount");
                            String bidderName = (String) payload.get("bidderName");

                            //trong javafx, chỉ có luồng chính mới có thể sửa UI, gọi platform runlater để gọi luồng chính cập nhật giao diện
                            Platform.runLater(() -> {
                                currentPrice_tf.setText(Double.toString(newPrice));
                                highestPayer_tf.setText(bidderName);
                            });
                        }

                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
