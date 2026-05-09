package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.UIUtils;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.*;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.IOException;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;


public class LiveAuctionController implements Initializable, ResponseListener {
    private volatile boolean running = true;
    Auction currentAuction;
    ServerConnection connection = NetworkManager.getConnection();
    @FXML private TextField priceInput_tf;
    @FXML private Label highestPayer_tf;
    @FXML private Label currentPrice_tf;
    @FXML private Label stepPrice_tf;
    @FXML private Label itemNameLb;
    @FXML private ImageView itemImageView;
    @FXML private Button placeBidButton;
    @FXML private Label lbTimeLeft;
    @FXML private LineChart<Number, Number> priceChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private XYChart.Series<Number, Number> priceSeries;
    @FXML private ListView<BidTransaction> bidHistory;
    @FXML private TextField autoMaxPrice_tf; // Ô nhập giá trần
    @FXML private ToggleButton autoBidToggle; // Nút bật/tắt chế độ tự động

    private ObservableList<BidTransaction> observable = FXCollections.observableArrayList();
    private TimeLeft timer;


    @FXML
    public void goMenu(ActionEvent event) throws IOException{
        timer.stop();
        running = false; //đóng while khi chuyển sang màn khác
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/SceneMain.fxml");
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(PlaceBidCommand.class, this);
    }
    @FXML
    public void clickToGoProductDisplayInfo(ActionEvent event) throws IOException{
        timer.stop();
        changeScene(event , "/com/javfxtutorial/hethongdaugia/view/fxml/man_hinh_hien_thi_sp.fxml");
    }

    @FXML
    public void placeBid (ActionEvent event) throws IOException, ClassNotFoundException {
        System.out.println("Bạn vừa ấn placeBid");

        //lấy ttin của bid
        BidTransaction bid = new BidTransaction();
        bid.setBidderId(ClientModel.getInstance().getCurrentUser().getId());
        bid.setBidderName(ClientModel.getInstance().getCurrentUser().getName());
        bid.setAmount(Double.parseDouble(priceInput_tf.getText()));
        bid.setAuctionId(currentAuction.getAuctionId());
        bid.setTimestamp(LocalDateTime.now());


        Command cmd = new PlaceBidCommand();
        cmd.addData("bid", bid);
        connection.sendCommand(cmd);
        System.out.println("Đã send bidcommand");
        // gửi command
    }

    public void setBidHistorytoScene(){
        Command cmd = new GetBidHistoryCommand();
        cmd.addData("auctionId", currentAuction.getAuctionId());
        connection.sendCommand(cmd);
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.register(GetBidHistoryCommand.class, this);
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
        // register để nhận command của người khác nữa
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.register(PlaceBidCommand.class, this);
        //khi vào auction thì register
        currentAuction = ClientModel.getInstance().getCurrentAuction();
        Command cmd = new RegisterToAuctionCommand();
        cmd.addData("currentAuction", currentAuction);
        connection.sendCommand(cmd);

        setCurrentAuctionInfoToScene();
        setBidHistorytoScene();
        initializePriceChart();
        running = true;

        // thời gian còn lại
        timer = new TimeLeft(lbTimeLeft, currentAuction.getEndingTime());
        timer.setOnFinished(() -> { //Khi het h thif khoa nut dat gia lai
            placeBidButton.setDisable(true); //vo hieu hoa nut chuyen sang mau xam mo
            placeBidButton.setText("Đã kết thúc");
        });
        timer.start();
    }
    private void initializePriceChart() {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Diễn biến giá"); // Tên của đường dữ liệu trong chú thích
        priceChart.getData().add(priceSeries); // Gắn dữ liệu vào biểu đồ

        // (Tùy chọn) Cấu hình để biểu đồ đẹp hơn
        priceChart.setAnimated(false); // Tắt hiệu ứng động để cập nhật mượt mà
        priceChart.setCreateSymbols(false); // Ẩn các chấm tròn tại mỗi điểm dữ liệu
    }

    @FXML
    public void onAutoBidToggle(ActionEvent event) {
        if (autoBidToggle.isSelected()) {
            try {
                // Lấy giá tối đa từ giao diện
                double maxPrice = Double.parseDouble(autoMaxPrice_tf.getText());
                User nowUser = ClientModel.getInstance().getCurrentUser();
                // Tạo cấu hình Bot cho người dùng hiện tại
                AutoBidConfig config = new AutoBidConfig(nowUser.getId(), nowUser.getName(), currentAuction.getAuctionId(), maxPrice, true);

                // Gửi lệnh lên Server
                Command cmd = new AutoBidCommand();
                cmd.addData("autoBidConfig", config);
                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.register(AutoBidCommand.class, this);
                connection.sendCommand(cmd);

                // Tạm thời khóa ô nhập giá để tránh thay đổi khi bot đang chạy
                autoMaxPrice_tf.setDisable(true);
                autoBidToggle.setText("Bot đang chạy...");
            } catch (NumberFormatException e) {
                UIUtils.showAlert("Lỗi nhập liệu", "Vui lòng nhập một số tiền hợp lệ!");
                autoBidToggle.setSelected(false);
            }
        } else {
            // Xử lý khi người dùng tắt Bot
            autoMaxPrice_tf.setDisable(false);
            autoBidToggle.setText("AutoBid");

            stopAutoBid();
        }
    }
    private void stopAutoBid() {
        AutoBidConfig config = new AutoBidConfig();
        config.setUserId(ClientModel.getInstance().getCurrentUser().getId());
        config.setAuctionId(currentAuction.getAuctionId());
        config.setActive(false);

        Command cmd = new AutoBidCommand();
        cmd.addData("autoBidConfig", config);
        connection.sendCommand(cmd);
    }


    @Override
    public void onResponse(Response rp) {
        if (rp.getCommand().getClass() == PlaceBidCommand.class) {
            BidTransaction bid = (BidTransaction) rp.getPayLoad();

            //nếu là người gửi thì hiện popup thông báo
            if (ClientModel.getInstance().getCurrentUser().getName().equals(bid.getBidderName())) {
                Platform.runLater(() -> {
                                showAlert("Trạng thái đặt bid", rp.getMessage());
                });
            }

            // nếu đặt giá thành công thì set up lại view
            if (rp.isSuccess()) {
                double newPrice = bid.getAmount();
                String bidderName = bid.getBidderName();
                int bidderId = bid.getBidderId();

                currentAuction.setCurrentPrice(newPrice);
                currentAuction.setWinnerId(bidderId);
                currentAuction.setWinningPrice(newPrice);
                //
                if (bid.getNewEndingTime() != bid.getTimestamp()) {
                    LocalDateTime newEnd = bid.getNewEndingTime();
                    currentAuction.setEndingTime(newEnd);

                    Platform.runLater(() -> { // Khởi động lại tgian ms
                        timer.stop();
                        timer = new TimeLeft(lbTimeLeft, newEnd);
                        timer.setOnFinished(() -> {
                            placeBidButton.setDisable(true);
                            placeBidButton.setText("Đã kết thúc");
                        });
                        timer.start();
                    });
                }

                Platform.runLater(() -> {
                    observable.add(bid); // Thêm vào ListView
                    //set lại giá
                    currentPrice_tf.setText(String.format("%,.0f VND", newPrice));
                    highestPayer_tf.setText(bidderName);

                    // VẼ điểm mới lên LineChart theo số thứ tự lượt bid
                    int bidSequenceNumber = observable.size();
                    XYChart.Data<Number, Number> newDataPoint = new XYChart.Data<>(bidSequenceNumber, newPrice);
                    priceSeries.getData().add(newDataPoint);
                    });
                }
            }
        if (rp.getCommand().getClass() == GetBidHistoryCommand.class) {
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.unregister(GetBidHistoryCommand.class, this);
            if (rp.isSuccess()) {
                ArrayList<BidTransaction> bidList = (ArrayList<BidTransaction>) rp.getPayLoad();
                // 1. Đảo ngược danh sách để hiển thị từ cũ đến mới
                java.util.Collections.reverse(bidList);
                Platform.runLater(() -> {
                                // CHỈ vẽ lịch sử nếu biểu đồ đang trống
                    if (priceSeries.getData().isEmpty()) {
                                    observable.setAll(bidList); // Dùng setAll thay vì addAll để tránh trùng lặp nếu có

                                    // Vẽ toàn bộ lịch sử
                        for (int i = 0; i < observable.size(); i++) {
                            BidTransaction historicalBid = observable.get(i);
                                        // i + 1 là số thứ tự lượt đặt giá (từ 1, 2, 3...)
                            priceSeries.getData().add(new XYChart.Data<>(i + 1, historicalBid.getAmount()));
                        }
                    }
                });
            }
        }
        if (rp.getCommand().getClass() == AutoBidCommand.class) {
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.unregister(AutoBidCommand.class, this);
            Platform.runLater(() -> {
                UIUtils.showAlert("Hệ thống AutoBid", rp.getMessage());
            });
        }
    }
}
