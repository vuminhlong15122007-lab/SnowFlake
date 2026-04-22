package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetBidHistoryCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.IOException;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;


public class LiveAuctionController implements Initializable {
    private volatile boolean running = true;
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

    @FXML private ListView<BidTransaction> bidHistory;
    private ObservableList<BidTransaction> observable = FXCollections.observableArrayList();

    private TimeLeft timer; //

    @FXML
    public void goMenu(ActionEvent event) throws IOException{
        timer.stop();
        running = false; //đóng while khi chuyển sang màn khác
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
        bid.setBidderName(ClientModel.getInstance().getCurrentUser().getName());
        bid.setAmount(Double.parseDouble(priceInput_tf.getText()));
        bid.setAuctionId(ClientModel.getInstance().getCurrentAuction().getAuctionId());
        bid.setTimestamp(LocalDateTime.now());



        Command cmd = new PlaceBidCommand();
        cmd.addData("bid", bid);
        cmd.addData("currentAuction", currentAuction);
        connection.sendCommand(cmd);
        System.out.println("Đã send command");
    }

    public void setBidHistorytoScene(){
        //hiển thị lsu đấu giá (nếu có)
        Command cmd = new GetBidHistoryCommand();
        cmd.addData("auctionId", currentAuction.getAuctionId());
        connection.sendCommand(cmd);
        bidHistory.setItems(observable);
        bidHistory.setCellFactory((ListView<BidTransaction> listview) -> new BidTransactionCell());
    }

    public void setCurrentAuctionInfoToScene(){
        // các thông tin cơ bản của phiên đấu gias
        currentAuction = ClientModel.getInstance().getCurrentAuction();
        currentPrice_tf.setText(String.format("%,.0f VND", currentAuction.getCurrentPrice()));
        stepPrice_tf.setText(String.format("%,.0f VND", currentAuction.getStepPrice()));
        highestPayer_tf.setText(String.valueOf(currentAuction.getWinnerId()));
        itemNameLb.setText(ClientModel.getInstance().getCurrentItem().getName());
        String base64Data = currentAuction.getItem().getImage();
        ImageHelper.loadBase64ToImageView(itemImageView,base64Data);
        System.out.println("Đã load xong giao diện");
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setCurrentAuctionInfoToScene();
        setBidHistorytoScene();
        connectToServer();
        running = true;

        // thời gian còn lại
        timer = new TimeLeft(lbTimeLeft, currentAuction.getEndingTime());
        timer.setOnFinished(() -> { //Khi het h thif khoa nut dat gia lai
            placeBidButton.setDisable(true); //vo hieu hoa nut chuyen sang mau xam mo
            placeBidButton.setText("Đã kết thúc");
        });
        timer.start();
    }

    private void connectToServer(){ //Khởi tạo một luồng riêng để luôn nhận phản hồi từ server mà không gây lag, đơ)
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    Response rp = connection.receiveResponse();
                    if (rp.getCommand() instanceof PlaceBidCommand){ //liên tục cập nhật giá cao nhất
                        //lấy giá mới từ server trả về (mỗi khi có client đặt giá, server sẽ thông báo cho tất cả client đang hoạt động)
                        BidTransaction bid = (BidTransaction) rp.getPayLoad();
                        if (ClientModel.getInstance().getCurrentUser().getName().equals(bid.getBidderName())){
                            Platform.runLater(() -> {
                            showAlert("Trạng thái đặt bid", rp.getMessage());});
                        }

                        if (rp.isSuccess()) {
                            if (bid.getAuctionId() == currentAuction.getAuctionId()) { //kiểm tra xem có trùng id auction không
                                double newPrice = bid.getAmount();
                                String bidderName = bid.getBidderName();
                                int bidderId = bid.getBidderId();

                                currentAuction.setCurrentPrice(newPrice);
                                currentAuction.setWinnerId(bidderId);
                                currentAuction.setWinningPrice(newPrice);


                                //trong javafx, chỉ có luồng chính mới có thể sửa UI, gọi platform runlater để gọi luồng chính cập nhật giao diện
                                Platform.runLater(() -> {
                                    observable.add(bid);
                                    currentPrice_tf.setText(Double.toString(newPrice));
                                    highestPayer_tf.setText(bidderName);
                                });
                            }
                        }

                    }

                    if (rp.getCommand() instanceof GetBidHistoryCommand){
                        if (rp.isSuccess()) {
                            ArrayList<BidTransaction> bidList = (ArrayList<BidTransaction>) rp.getPayLoad();
                            Platform.runLater(() -> {observable.addAll(bidList);});
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
