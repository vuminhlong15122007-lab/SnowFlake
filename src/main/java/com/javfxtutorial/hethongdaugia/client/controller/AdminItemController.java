package com.javfxtutorial.hethongdaugia.client.controller;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteUserCommand;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminItemMangementController implements  Initializable {
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item,Integer> colId ;
    @FXML private TableColumn<Item,String> colItemName;
    @FXML private TableColumn<Item,String> colStartPrice;
    @FXML private TableColumn<Item,String> colStepPrice;
    @FXML private TableColumn<Item,String> colCategory;
    @FXML private TableColumn<Item,String> colOwner;
    @FXML private TableColumn<Item,String> colStatus;

    private ItemDAO  itemDAO = ItemDAO.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("status")); //truyền bừa
        colOwner.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("initPrice"));
        colStepPrice.setCellValueFactory(new PropertyValueFactory<>("stepPrice"));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getDesciption()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        try {
            loadItemData();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadItemData() throws IOException, ClassNotFoundException {
        Command cmd = new GetAllAuctionsCommand();
        ServerConnection connection = new ServerConnection();
        connection.sendCommand(cmd);
        Response rp = connection.receiveResponse();
        connection.close();

        ArrayList<Auction> auctionlist = (ArrayList<Auction>) rp.getPayLoad();
        ObservableList<Auction> danhSach = FXCollections.observableArrayList(auctionlist);
        itemTable.setItems(danhSach);
    }

    public void clickButtonExit(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/SceneMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clickToDeleteItem() throws IOException, ClassNotFoundException {
        ServerConnection connection = new ServerConnection();
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
            DeleteItemCommand cmd = new DeleteItemCommand(selectItem.getItemId());
            try{
            connection.sendCommand(cmd);
            Response rp = connection.receiveResponse();

            if (rp.isSuccess()) {
                showAlert("Xóa thành công", rp.getMessage());
                loadItemData();//load lai bang
            } else {
                showAlert("Lỗi", rp.getMessage());
            }

            }catch(Exception e){}

        connection.close();
    }


}private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }}
