package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.Util.TimeLeft;
import com.javfxtutorial.hethongdaugia.client.Util.ToastNotifier;
import com.javfxtutorial.hethongdaugia.client.Util.UIUtils;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.*;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveAuctionController implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(LiveAuctionController.class);

  volatile boolean running = true;
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
  @FXML private Label auctionStatusLabel; // Nhãn trạng thái phiên (chỉ hiện cho admin)
  @FXML private Button btnToInformation;
  @FXML private NotificationToastController notificationToastController;
  private boolean isAdmin = false;
  private boolean isSeller = false;
  private final NetworkManager networkManager = NetworkManager.getInstance();

  private final ObservableList<BidTransaction> observable = FXCollections.observableArrayList();
  private TimeLeft timer;

  public LiveAuctionController() throws ConnectionFailedException {}

  @FXML
  public void goMenu(ActionEvent event) {
    timer.stop();
    running = false;
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.unregister(PlaceBidCommand.class, this);
    networkManager.unregister(AutoBidCommand.class, this);
    networkManager.unregister(UpdateAuctionCommand.class, this);
    networkManager.unregister(UpdateAuctionStatusCommand.class, this);
    if (isAdmin) {
      changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_ProductManagement.fxml");
    } else if (isSeller) {
      changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Seller_ProductManagement.fxml");
    } else if (ClientModel.getInstance().isFromParticipatedAuction) {
      changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/UserParticipatedAuction.fxml");
      ClientModel.getInstance().isFromParticipatedAuction = false;
    } else {
      changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml");
    }
  }

  @FXML
  public void clickToGoProductDisplayInfo(ActionEvent event) {
    timer.stop();
    running = false;
    networkManager.unregister(PlaceBidCommand.class, this);
    networkManager.unregister(AutoBidCommand.class, this);
    networkManager.unregister(UpdateAuctionCommand.class, this);
    networkManager.unregister(UpdateAuctionStatusCommand.class, this);
    changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionInformation.fxml");
  }

  @FXML
  public void placeBid() {
    log.info("Bạn vừa ấn placeBid");

    try {
      // 1. Lấy text và loại bỏ các dấu phẩy, khoảng trắng (nếu người dùng có nhập)
      String rawInput = priceInput_tf.getText().trim();
      if (rawInput.isEmpty()) {
        UIUtils.showAlert("Lỗi đặt giá", "Vui lòng nhập số tiền");
        return;
      }

      rawInput = rawInput.replace(",", ".");
      BigDecimal inputAmount = new BigDecimal(rawInput);

      // 2. Validate sớm ngay tại Client (Giảm tải cho Server)
      BigDecimal minRequired = currentAuction.getStepPrice().add(currentAuction.getCurrentPrice());
      if (inputAmount.compareTo(minRequired) < 0) {
        Platform.runLater(
            () -> {
              UIUtils.showAlert(
                  "Lỗi đặt giá",
                  String.format(
                      "Bạn phải đặt tối thiểu %,.0f VND (Giá hiện tại + Bước giá)", minRequired));
            });
        return;
      }

      // 3. Đóng gói gửi Server nếu qua cửa
      BidTransaction bid = new BidTransaction();
      bid.setBidderId(ClientModel.getInstance().getCurrentUser().getId());
      bid.setBidderName(ClientModel.getInstance().getCurrentUser().getName());
      bid.setAmount(inputAmount);
      bid.setAuctionId(currentAuction.getAuctionId());
      bid.setTimestamp(LocalDateTime.now());

      Command cmd = new PlaceBidCommand();
      cmd.addData("bid", bid);
      NetworkManager.getInstance().sendRequest(cmd, this);
      log.info("Đã send bidcommand với giá: {}", inputAmount);

    } catch (NumberFormatException e) {
      Platform.runLater(
          () -> {
            showAlert("Lỗi", "Vui lòng nhập số tiền hợp lệ");
          });
    } catch (SendFailedException e) {
      Platform.runLater(
          () -> {
            showAlert("Lỗi gửi", "Không thể gửi yêu cầu đặt giá");
          });
    } catch (Exception e) {
      log.error("Lỗi đặt giá: {}", e.getMessage(), e);
      Platform.runLater(
          () -> {
            showAlert("Lỗi", "Đặt giá thất bại: " + e.getMessage());
          });
    }
  }

  public void setBidHistorytoScene() throws SendFailedException, ConnectionFailedException {
    Command cmd = new GetBidHistoryCommand();
    cmd.addData("auctionId", currentAuction.getAuctionId());
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.sendRequest(cmd, this);
    bidHistory.setItems(observable);
    bidHistory.setCellFactory((ListView<BidTransaction> _) -> new BidTransactionCell());
  }

  public void setCurrentAuctionInfoToScene() {
    // các thông tin cơ bản của phiên đấu gias
    currentAuction = ClientModel.getInstance().getCurrentAuction();
    currentPrice_tf.setText(String.format("%,.0f VND", currentAuction.getCurrentPrice()));
    stepPrice_tf.setText(String.format("%,.0f VND", currentAuction.getStepPrice()));
    highestPayer_tf.setText(currentAuction.getWinnerName());
    itemNameLb.setText(currentAuction.getItem().getName());
    String base64Data = currentAuction.getItem().getImage();
    ImageHelper.loadBase64ToImageView(itemImageView, base64Data);
    log.info("Đã load xong giao diện");
  }

  @FXML
  public void initialize() throws SendFailedException, ConnectionFailedException {
    currentAuction = ClientModel.getInstance().getCurrentAuction();
    // Xác định xem người dùng hiện tại có phải admin không
    isAdmin = ClientModel.getInstance().getCurrentUser().getAccountType() == AccountType.ADMIN;
    //Xac dinh có phải seller k
    isSeller = ClientModel.getInstance().getCurrentUser().getId() == currentAuction.getSellerId();

    // register để nhận command của người khác nữa
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.register(PlaceBidCommand.class, this);
    networkManager.register(AutoBidCommand.class, this);
    networkManager.register(UpdateAuctionCommand.class, this);
    networkManager.register(UpdateAuctionStatusCommand.class, this);
    // khi vào auction thì register
    Command cmd = new RegisterToAuctionCommand();
    cmd.addData("currentAuction", currentAuction);
    connection.sendCommand(cmd);

    setCurrentAuctionInfoToScene();
    setBidHistorytoScene();
    initializePriceChart();
    running = true;

    // Cài đặt giao diện dành cho admin
    setupAdminView();
    //cai dat giao diện cho seller
    setupSellerView();
    // thời gian còn lại
    timer = new TimeLeft(lbTimeLeft, currentAuction.getEndingTime());
    timer.setOnFinished(
        () -> {
          placeBidButton.setDisable(true);
          placeBidButton.setText("End");
          autoBidToggle.setDisable(true);
          priceInput_tf.setDisable(true);
          autoMaxPrice_tf.setDisable(true);
        });
    timer.start();
    if (currentAuction.getStatus() == AuctionStatus.CANCELLED_BY_ADMIN) {
      timer.stop();
      lbTimeLeft.setText("00:00:00");
      placeBidButton.setDisable(true);
      placeBidButton.setText("Đã kết thúc");
      autoBidToggle.setDisable(true);
      priceInput_tf.setDisable(true);
      autoMaxPrice_tf.setDisable(true);
    }
  }

  //  Thiết lập giao diện theo vai trò: Admin: CHỈ XEM — ko đc
  // đặt giá.Lý do: nếu admin thắng,  thanh toán thế nào cho admin
  // phiên sẽ bị hủy sau 24h,seller mất trắng. Admin vào để giám sát lịch sử + đồ thị,

  private void setupAdminView() {
    if (!isAdmin) return;

    AuctionStatus status = currentAuction.getStatus();

    // Hiện nhãn trạng thái phiên
    if (auctionStatusLabel != null) {
      auctionStatusLabel.setVisible(true);
      String statusText;
      String statusStyle;
      switch (status) {
        case RUNNING -> {
          statusText = "ĐANG DIỄN RA";
          statusStyle = "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        case NOT_START -> {
          statusText = "CHƯA BẮT ĐẦU";
          statusStyle = "-fx-text-fill: -sf-accent; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        case CLOSED -> {
          statusText = "ĐÃ KẾT THÚC";
          statusStyle = "-fx-text-fill: -sf-text; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        case CANCELLED -> {
            statusText = "ĐÃ BỊ HỦY";
            statusStyle = "-fx-text-fill: -sf-danger; -fx-font-weight: bold; -fx-font-size: 13px;";

        }case CANCELLED_BY_ADMIN -> {
          statusText = "ĐÃ BỊ HỦY  ";
          statusStyle = "-fx-text-fill: -sf-danger; -fx-font-weight: bold; -fx-font-size: 13px;";
        }default -> {
          statusText = "ĐÃ THÀNH CÔNG";
          statusStyle = "-fx-text-fill: -sf-text; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
      }
      auctionStatusLabel.setText(statusText);
      auctionStatusLabel.setStyle(statusStyle);
    }

    placeBidButton.setDisable(true);
    placeBidButton.setText("Không được đặt");
    autoBidToggle.setDisable(true);
    priceInput_tf.setDisable(true);
    autoMaxPrice_tf.setDisable(true);
    btnToInformation.setDisable(true);
  }
  private void setupSellerView() {
    if (!isSeller) return;
    AuctionStatus status = currentAuction.getStatus();

    // Hiện nhãn trạng thái phiên
    if (auctionStatusLabel != null) {
      auctionStatusLabel.setVisible(true);
      String statusText;
      String statusStyle;
      switch (status) {
        case RUNNING -> {
          statusText = "ĐANG DIỄN RA";
          statusStyle = "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        case NOT_START -> {
          statusText = "CHƯA BẮT ĐẦU";
          statusStyle = "-fx-text-fill: -sf-accent; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        case CLOSED -> {
          statusText = "ĐÃ KẾT THÚC";
          statusStyle = "-fx-text-fill: -sf-text; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        case CANCELLED -> {
          statusText = "ĐÃ BỊ HỦY";
          statusStyle = "-fx-text-fill: -sf-danger; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
        default -> {
          statusText = "ĐÃ THÀNH CÔNG";
          statusStyle = "-fx-text-fill: -sf-text; -fx-font-weight: bold; -fx-font-size: 13px;";
        }
      }
      auctionStatusLabel.setText(statusText);
      auctionStatusLabel.setStyle(statusStyle);
    }
    placeBidButton.setDisable(true);
    placeBidButton.setText("Không được đặt");
    autoBidToggle.setDisable(true);
    priceInput_tf.setDisable(true);
    autoMaxPrice_tf.setDisable(true);
  }

  private void initializePriceChart() {
    priceSeries = new XYChart.Series<>();
    priceSeries.setName("Diễn biến giá"); // Tên của đường dữ liệu trong chú thích
    priceChart.getData().add(priceSeries); // Gắn dữ liệu vào biểu đồ

    // (Tùy chọn) Cấu hình để biểu đồ đẹp hơn
    priceChart.setAnimated(false); // Tắt hiệu ứng động để cập nhật mượt mà
    priceChart.setCreateSymbols(false); // Ẩn các chấm tròn tại mỗi điểm dữ liệu
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(1);
    xAxis.setUpperBound(1);
    xAxis.setTickUnit(1);
    xAxis.setMinorTickVisible(false);
    xAxis.setTickLabelFormatter(
        new StringConverter<>() {
          @Override
          public String toString(Number number) {
            return String.format("%.0f", number.doubleValue());
          }

          @Override
          public Number fromString(String string) {
            return Integer.parseInt(string);
          }
        });
    yAxis.setTickLabelFormatter(
        new StringConverter<>() {
          @Override
          public String toString(Number number) {
            return String.format("%,.0f", number.doubleValue());
          }

          @Override
          public Number fromString(String string) {
            return new BigDecimal(string.replace(",", ""));
          }
        });
  }

  private void updatePriceChartXAxis() {
    int pointCount = priceSeries.getData().size();
    xAxis.setLowerBound(1);
    xAxis.setUpperBound(Math.max(1, pointCount));
    xAxis.setTickUnit(1);
  }

  @FXML
  public void onAutoBidToggle() throws SendFailedException, ConnectionFailedException {
    if (autoBidToggle.isSelected()) {
      try {
        // Lấy giá tối đa từ giao diện
        BigDecimal maxPrice = new BigDecimal(autoMaxPrice_tf.getText());
        User nowUser = ClientModel.getInstance().getCurrentUser();
        // Tạo cấu hình Bot cho người dùng hiện tại
        AutoBidConfig config =
            new AutoBidConfig(
                nowUser.getId(), nowUser.getName(), currentAuction.getAuctionId(), maxPrice, true);

        // Gửi lệnh lên Server
        Command cmd = new AutoBidCommand();
        cmd.addData("autoBidConfig", config);
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.sendRequest(cmd, this);

        // Tạm thời khóa ô nhập giá để tránh thay đổi khi bot đang chạy
        autoMaxPrice_tf.setDisable(true);
        autoBidToggle.setText("Autobid");
      } catch (NumberFormatException e) {
        toast().warning("Vui lòng nhập một số tiền hợp lệ!");
        autoBidToggle.setSelected(false);
      } catch (SendFailedException e) {
        log.error(e.getMessage());
        toast().error("Không thể gửi yêu cầu AutoBid");
      } catch (ConnectionFailedException e) {
        log.error(e.getMessage());
        toast().error("Không thể kết nối server");
      }
    } else {
      // Xử lý khi người dùng tắt Bot
      autoMaxPrice_tf.setDisable(false);
      autoBidToggle.setText("AutoBid");

      stopAutoBid();
      toast().info("Đã tắt AutoBid");
    }
  }

  private void stopAutoBid() throws SendFailedException, ConnectionFailedException {
    AutoBidConfig config = new AutoBidConfig();
    config.setUserId(ClientModel.getInstance().getCurrentUser().getId());
    config.setAuctionId(currentAuction.getAuctionId());
    config.setActive(false);

    Command cmd = new AutoBidCommand();
    cmd.addData("autoBidConfig", config);
    NetworkManager.getInstance().sendRequest(cmd, this);
  }

  public void showNotification(String message) {
    toast().success(message);
  }

  private ToastNotifier toast() {
    return ToastNotifier.of(notificationToastController);
  }

  @Override
  public void onResponse(Response rp) {
    if (rp.getCommand().getClass() == PlaceBidCommand.class) {
      BidTransaction bid = (BidTransaction) rp.getPayLoad();

      if (!rp.isSuccess()) {
        Platform.runLater(() -> toast().error(rp.getMessage()));
        return;
      }

      // Từ đây bid chắc chắn không null (vì success)
      if (bid == null) return;
      if (bid.getBidderId() == ClientModel.getInstance().getCurrentUser().getId()) {
        Platform.runLater(() -> showNotification(rp.getMessage()));
      }


      // nếu đặt giá thành công thì set up lại view
      if (rp.isSuccess()) {
        BigDecimal newPrice = bid.getAmount();
        String bidderName = bid.getBidderName();
        int bidderId = bid.getBidderId();

        Platform.runLater(
            () -> {
              // 1. Xử lý lịch sử (ListView):
              int insertIndex = 0;
              while (insertIndex < observable.size()
                  && observable.get(insertIndex).getAmount().compareTo(bid.getAmount()) > 0) {
                insertIndex++;
              }
              observable.add(insertIndex, bid);
              if (observable.size() > 1000) { // giới hạn chỉ 1000 lịch sử gần nhất
                observable.remove(1000, observable.size());
              }

              // dòng này sẽ dùng để sort lại bảng
              // FXCollections.sort(observable, (b1, b2) ->
              // Double.compare(b2.getAmount(), b1.getAmount()));

              // 2. CHỐNG ẢO GIÁC ĐI LÙI (Bảo vệ giao diện chính)
              // Chỉ cho phép cập nhật thông tin chung khi giá nhận được LỚN HƠN giá
              // đang hiển thị
              if (newPrice.compareTo(currentAuction.getCurrentPrice()) > 0) {

                // Cập nhật lại Model đang lưu trong RAM
                currentAuction.setCurrentPrice(newPrice);
                currentAuction.setWinningPrice(newPrice);
                currentAuction.setWinnerName(bidderName);

                // Cập nhật các Label hiển thị bên trái màn hình
                currentPrice_tf.setText(String.format("%,.0f VND", newPrice));
                highestPayer_tf.setText(bidderName);

                // Cập nhật Biểu đồ đường (LineChart)
                int bidSequenceNumber = priceSeries.getData().size() + 1;
                XYChart.Data<Number, Number> newDataPoint =
                    new XYChart.Data<>(bidSequenceNumber, newPrice);
                priceSeries.getData().add(newDataPoint);
                updatePriceChartXAxis();

                // Cập nhật lại đồng hồ đếm ngược nếu có gia hạn (Anti-snipe)
                // Lưu ý: Dùng .equals() để so sánh thời gian thay vì !=
                if (bid.getNewEndingTime() != null
                    && !bid.getNewEndingTime().equals(bid.getTimestamp())) {
                  LocalDateTime newEnd = bid.getNewEndingTime();
                  currentAuction.setEndingTime(newEnd);

                  if (timer != null) timer.stop(); // Dừng bộ đếm cũ
                  timer = new TimeLeft(lbTimeLeft, newEnd);
                  timer.setOnFinished(
                      () -> {
                        placeBidButton.setDisable(true);
                        placeBidButton.setText("Đã kết thúc");
                      });
                  timer.start();
                }
              } else {
                // (Tùy chọn) In log ra console để bạn dễ theo dõi những luồng dữ
                // liệu bị chậm
                log.info(
                    "Đã chặn gói tin tới muộn: {} nhỏ hơn giá hiện tại {}",
                    newPrice,
                    currentAuction.getCurrentPrice());
              }
            });
      }
    }
    if (rp.getCommand().getClass() == GetBidHistoryCommand.class) {
      NetworkManager networkManager = NetworkManager.getInstance();
      networkManager.unregister(GetBidHistoryCommand.class, this);

      if (rp.isSuccess()) {
        ArrayList<BidTransaction> bidList = (ArrayList<BidTransaction>) rp.getPayLoad();

        Platform.runLater(
            () -> {
              if (priceSeries.getData().isEmpty()) {

                // LẬT NGƯỢC HIỂN THỊ Ở ĐÂY:
                // Sắp xếp danh sách lịch sử theo Giá.
                // - Dùng b2 so sánh b1: Giá cao nhất (mới nhất) nằm TRÊN CÙNG.
                // - Nếu bạn muốn Giá cao nhất nằm DƯỚI CÙNG, đổi thành:
                // Double.compare(b1.getAmount(), b2.getAmount())
                bidList.sort((b1, b2) -> b2.getAmount().compareTo(b1.getAmount()));

                observable.setAll(bidList);

                // VẼ BIỂU ĐỒ:
                // Vì danh sách observable đang xếp Giá Cao -> Giá Thấp (Mới -> Cũ)
                // Để biểu đồ vẽ đúng chiều thời gian đi tới (Cũ -> Mới), ta phải
                // duyệt mảng ngược từ dưới lên trên.
                int soThuTuLuotBid = 1;
                for (int i = observable.size() - 1; i >= 0; i--) {
                  BidTransaction historicalBid = observable.get(i);
                  priceSeries
                      .getData()
                      .add(new XYChart.Data<>(soThuTuLuotBid, historicalBid.getAmount()));
                  soThuTuLuotBid++;
                }
                updatePriceChartXAxis();
              }
            });
      }
    }
    if (rp.getCommand().getClass() == AutoBidCommand.class) {
      String message =
          "AUTO_BID_TIE_ALERT".equals(rp.getMessage())
              ? String.valueOf(rp.getPayLoad())
              : rp.getMessage();
      Platform.runLater(
          () -> {
            if ("AUTO_BID_TIE_ALERT".equals(rp.getMessage())) {
              toast().warning(message);
            } else if (rp.isSuccess()) {
              toast().info(message);
            } else {
              toast().error(message);
            }
          });
    }

    if (rp.getCommand().getClass() == UpdateAuctionCommand.class) {
      if (rp.isSuccess() && rp.getPayLoad() instanceof Auction updatedAuction) {
        Platform.runLater(() -> applyAuctionSnapshot(updatedAuction));
      }
    }

    if (rp.getCommand().getClass() == UpdateAuctionStatusCommand.class) {
      NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);
      if ("ADMIN_CANCELLED_AUCTION".equals(rp.getMessage())) {
        Platform.runLater(
            () -> {
              if (timer != null) timer.stop();
              Stage stage = (Stage) lbTimeLeft.getScene().getWindow();
              showAlert("Thông báo", "Phiên đấu giá đã bị admin hủy.");
              try {
                Parent root =
                    FXMLLoader.load(
                        getClass()
                            .getResource(
                                "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionList.fxml"));
                stage.setScene(new Scene(root));
              } catch (Exception e) {
                log.error("Lỗi chuyển scene: {}", e.getMessage());
              }
            });
      }
    }
  }

  private void applyAuctionSnapshot(Auction updatedAuction) {
    if (updatedAuction == null || currentAuction == null) {
      return;
    }
    if (updatedAuction.getAuctionId() != currentAuction.getAuctionId()) {
      return;
    }

    BigDecimal oldPrice = currentAuction.getCurrentPrice();
    BigDecimal newPrice = updatedAuction.getCurrentPrice();
    LocalDateTime oldEndingTime = currentAuction.getEndingTime();
    currentAuction = updatedAuction;
    ClientModel.getInstance().setCurrentAuction(updatedAuction);

    if (newPrice != null) {
      currentPrice_tf.setText(String.format("%,.0f VND", newPrice));
      if (oldPrice == null || newPrice.compareTo(oldPrice) > 0) {
        int bidSequenceNumber = priceSeries.getData().size() + 1;
        priceSeries.getData().add(new XYChart.Data<>(bidSequenceNumber, newPrice));
        updatePriceChartXAxis();
      }
    }
    highestPayer_tf.setText(updatedAuction.getWinnerName());
    if (updatedAuction.getEndingTime() != null
        && timer != null
        && !updatedAuction.getEndingTime().equals(oldEndingTime)) {
      timer.stop();
      timer = new TimeLeft(lbTimeLeft, updatedAuction.getEndingTime());
      timer.start();
    }
  }
}
