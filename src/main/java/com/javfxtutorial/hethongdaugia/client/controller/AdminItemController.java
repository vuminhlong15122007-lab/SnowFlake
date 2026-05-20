package com.javfxtutorial.hethongdaugia.client.controller;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

import com.javfxtutorial.hethongdaugia.common.model.domain.AuctionModificationManager;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminItemController implements ResponseListener {
    @FXML private TableView<Auction> itemTable;
    @FXML private TableColumn<Auction, Integer> colId;
    @FXML private TableColumn<Auction, String> colItemName;
    @FXML private TableColumn<Auction, String> colStartPrice;
    @FXML private TableColumn<Auction, String> colStepPrice;
    @FXML private TableColumn<Auction, String> colCategory;
    @FXML private TableColumn<Auction, String> colOwner;
    @FXML private TableColumn<Auction, String> colStatus;
    private ObservableList<Auction> observableList;
    private static final Logger log = LoggerFactory.getLogger(AdminItemController.class);


    @FXML
    public void initialize() {
        observableList = ClientModel.getInstance().getAllAuctions();
        itemTable.setItems(observableList);

        colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colItemName.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colStartPrice.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.format("%,.0f VND", cellData.getValue().getCurrentPrice())));
        colStepPrice.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.format("%,.0f VND", cellData.getValue().getStepPrice())));
        colCategory.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getItem().getCategory().name()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        if (!AuctionModificationManager.getInstance().isAllAuctionsLoaded) {
            new Thread(() -> {
                try {
                    loadItemData();
                } catch (Exception e) {
                    log.error("Lỗi load data: {}", e.getMessage(), e);
                    Platform.runLater(() -> showAlert("Lỗi", "Tải dữ liệu thất bại"));
                }
            }).start();
        }
    }

    private void loadItemData()
            throws IOException,
                    ClassNotFoundException,
                    SendFailedException,
                    ConnectionFailedException {
        Command cmd = new GetAllAuctionsCommand();
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.sendRequest(cmd, this);
    }

    public void clickButtonExit(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }

    public void clickToGoUserAdmin(ActionEvent event) {
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_UserManagement.fxml");
    }

    @FXML
    public void clickToDeleteItem() throws SendFailedException, ConnectionFailedException {
        Auction selectItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectItem == null) {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm cần xóa");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn chắc chắn muốn xóa sản phẩm?");
        confirmAlert.setContentText(
                "Sản phẩm: "
                        + selectItem.getItem().getName()
                        + "\nDescription: "
                        + selectItem.getItem().getDescription()
                        + "\nID người bán: "
                        + selectItem.getSellerId());
        ButtonType yes = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        // gan 2 nut vao thay ok, cancle mac dinh
        confirmAlert.getButtonTypes().setAll(yes, no);
        // cho nguoi dung bam
        ButtonType result = confirmAlert.showAndWait().orElse(null);
        // neu co
        if (result == yes) {
            DeleteAuctionCommand cmd = new DeleteAuctionCommand(selectItem);
            new Thread(() -> {
                try {
                    NetworkManager.getInstance().sendRequest(cmd, this);
                } catch (Exception e) {
                    log.error("Lỗi xóa auction: {}", e.getMessage(), e);
                    Platform.runLater(() -> showAlert("Lỗi", "Không thể gửi yêu cầu"));
                }
            }).start();
        }
    }
    @FXML
    public void clickToGoAuction(ActionEvent event) {
        Auction selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một sản phẩm.");
            return;
        }
        ClientModel.getInstance().setCurrentAuction(selected);
        changeScene(event, "/com/javfxtutorial/hethongdaugia/view/fxml/AuctionInformation.fxml");
    }
    @FXML
    public void clickToCancelAuction(ActionEvent event) {
        Auction selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một phiên đấu giá.");
            return;
        }
        if (selected.getStatus() == AuctionStatus.CLOSED
                || selected.getStatus() == AuctionStatus.CANCELLED) {
            showAlert("Thông báo", "Phiên này đã kết thúc hoặc đã bị hủy.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy");
        confirm.setHeaderText("Hủy phiên đấu giá?");
        confirm.setContentText("Sản phẩm: " + selected.getItem().getName());
        ButtonType yes = new ButtonType("Hủy phiên", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(result -> {
            if (result == yes) {
                try {
                    selected.setStatus(AuctionStatus.CANCELLED);
                    UpdateAuctionStatusCommand cmd = new UpdateAuctionStatusCommand(selected);
                    NetworkManager.getConnection().sendCommand(cmd);
                    NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
                } catch (Exception e) {
                    showAlert("Lỗi", "Không thể hủy phiên: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onResponse(Response rp) {
        if (rp.getCommand().getClass() == DeleteAuctionCommand.class) {
            if (rp.isSuccess()) {
                Platform.runLater(() -> {
                    showAlert("Xóa thành công", rp.getMessage(), "FunnyCat.gif");
                });
                try {
                    loadItemData(); // load lai bang
                } catch (IOException
                        | ClassNotFoundException
                        | SendFailedException
                        | ConnectionFailedException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                Platform.runLater(() -> {
                    showAlert("Lỗi", rp.getMessage(), "Wrong.gif");
                });
            }
            NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.unregister(DeleteAuctionCommand.class, this);
        }
        if (rp.getCommand().getClass() == UpdateAuctionStatusCommand.class) {
            NetworkManager.getInstance().unregister(UpdateAuctionStatusCommand.class, this);
            Platform.runLater(() -> {
                if (rp.isSuccess()) {
                    showAlert("Thành công", "Đã hủy phiên đấu giá.", "FunnyCat.gif");
                    try { loadItemData(); } catch (Exception e) { e.printStackTrace(); }
                } else {
                    showAlert("Lỗi", rp.getMessage(), "Wrong.gif");
                }
            });}
        if (rp.getCommand().getClass() == GetAllAuctionsCommand.class) {
            Platform.runLater(() -> {
                if (rp.isSuccess()) {
                    ArrayList<Auction> auctionlist = (ArrayList<Auction>) rp.getPayLoad();
                    AuctionModificationManager.getInstance().isAllAuctionsLoaded = true;
                    observableList.setAll(auctionlist);
                }
            });
            NetworkManager.getInstance().unregister(GetAllAuctionsCommand.class, this);
        }
    }
}
