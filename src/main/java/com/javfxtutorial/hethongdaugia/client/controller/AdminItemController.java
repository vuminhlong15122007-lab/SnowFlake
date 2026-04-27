package com.javfxtutorial.hethongdaugia.client.controller;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAllAuctionsCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;
import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.showAlert;

public class AdminItemController implements  Initializable {
    @FXML private TableView<Auction> itemTable;
    @FXML private TableColumn<Auction,Integer> colId ;
    @FXML private TableColumn<Auction,String> colItemName;
    @FXML private TableColumn<Auction,String> colStartPrice;
    @FXML private TableColumn<Auction,String> colStepPrice;
    @FXML private TableColumn<Auction,String> colCategory;
    @FXML private TableColumn<Auction,String> colOwner;
    @FXML private TableColumn<Auction,String> colStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("status")); //truyền bừa
        colOwner.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("initPrice"));
        colStepPrice.setCellValueFactory(new PropertyValueFactory<>("stepPrice"));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getDescription()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        try {
            loadItemData();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadItemData() throws IOException, ClassNotFoundException {
        Command cmd = new GetAllAuctionsCommand();
        ServerConnection connection = ServerConnection.getInstance();
        connection.sendCommand(cmd);
        Response rp = connection.receiveResponse();
        ArrayList<Auction> auctionlist = (ArrayList<Auction>) rp.getPayLoad();
        ObservableList<Auction> danhSach = FXCollections.observableArrayList(auctionlist);
        itemTable.setItems(danhSach);
    }

    public void clickButtonExit(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/login.fxml");
    }
    public void clickToGoUserAdmin(ActionEvent event){
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/Quan_Ly_User_Admin.fxml");
    }
    @FXML
    public void clickToDeleteItem() throws IOException, ClassNotFoundException {
        ServerConnection connection = ServerConnection.getInstance();
        Auction selectItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectItem == null) {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm cần xóa");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn chắc chắn muốn xóa sản phẩm?");
        confirmAlert.setContentText("Sản phẩm: " + selectItem.getItem().getName() +
                "\nDescription: " + selectItem.getItem().getDescription() +
                "\nID người bán: " + selectItem.getSellerId());
        ButtonType yes = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        //gan 2 nut vao thay ok, cancle mac dinh
        confirmAlert.getButtonTypes().setAll(yes, no);
        //cho nguoi dung bam
        ButtonType result = confirmAlert.showAndWait().orElse(null);
        //neu co
        if (result == yes) {
            DeleteAuctionCommand cmd = new DeleteAuctionCommand(selectItem);
            try{
            connection.sendCommand(cmd);
            Response rp = connection.receiveResponse();

            if (rp.isSuccess()) {
                showAlert("Xóa thành công", rp.getMessage() , "FunnyCat.gif");
                loadItemData();//load lai bang
            } else {
                showAlert("Lỗi", rp.getMessage() , "Wrong.gif");
            }

            }catch(Exception e){}
    }



}
}
