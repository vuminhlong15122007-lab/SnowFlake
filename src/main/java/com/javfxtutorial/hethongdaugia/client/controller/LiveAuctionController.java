package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;
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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;


public class LiveAuctionController implements Initializable {
    Auction currentAuction;
    ServerConnection connection = ServerConnection.getInstance();
    @FXML private TextField priceInput_tf;
    @FXML private Label highestPayer_tf;
    @FXML private Label currentPrice_tf;
    @FXML private Label stepPrice_tf;
    @FXML private Label itemNameLb;
    @FXML private ImageView itemImageView;
    @FXML private Button placeBidButton;
    @FXML private Label lbTimeLeft;
    private TimeLeft timer; //

    @FXML
    public void goMenu(ActionEvent event) throws IOException{
        timer.stop();
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");
    }
    @FXML
    public void clickToGoProductDisplayInfo(ActionEvent event) throws IOException{
        timer.stop();
        changeScene(event , "/com/javfxtutorial/hethongdaugia/view/fxml/man_hinh_hien_thi_sp.fxml");
    }

    @FXML
    public void placeBid (ActionEvent event) throws IOException, ClassNotFoundException {
        System.out.println("Bạn vừa ấn placeBid");
        BidTransaction bid = new BidTransaction();
        bid.setBidderId(ClientModel.getInstance().getCurrentUser().getId());
        bid.setAmount(Double.parseDouble(priceInput_tf.getText()));
        bid.setAuctionId(ClientModel.getInstance().getCurrentAuction().getAuctionId());
        bid.setTimestamp(LocalDateTime.now());



        Command cmd = new PlaceBidCommand();
        cmd.addData("bid", bid);
        cmd.addData("currentAuction", currentAuction);
        connection.sendCommand(cmd);
        System.out.println("Đã send command");
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentAuction = ClientModel.getInstance().getCurrentAuction();
        currentPrice_tf.setText(String.format("%,.0f VND", currentAuction.getCurrentPrice()));
        stepPrice_tf.setText(String.format("%,.0f VND", currentAuction.getCurrentPrice()));
        highestPayer_tf.setText(String.valueOf(currentAuction.getWinnerId()));
        itemNameLb.setText(ClientModel.getInstance().getCurrentItem().getName());
        String base64Data = currentAuction.getItem().getImage();
        ImageHelper.loadBase64ToImageView(itemImageView,base64Data);
        System.out.println("Đã load xong giao diện");
        connectToServer();
        timer = new TimeLeft(lbTimeLeft, currentAuction.getEndingTime());
        timer.setOnFinished(() -> { //Khi het h thif khoa nut dat gia lai
            placeBidButton.setDisable(true); //vo hieu hoa nut chuyen sang mau xam mo
            placeBidButton.setText("Đã kết thúc");
        });
        timer.start();
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
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
