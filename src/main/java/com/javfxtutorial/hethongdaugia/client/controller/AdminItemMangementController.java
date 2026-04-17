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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("email"));
        colOwner.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStepPrice.setCellValueFactory(new PropertyValueFactory<>("stepPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadItemData();
    }

    private void loadItemData(){
        ObservableList<Item> danhSach = FXCollections.observableArrayList(itemDAO.selectAll());
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
        Item selectItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectItem == null) {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm cần xóa");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn chắc chắn muốn xóa sản phẩm?");
        confirmAlert.setContentText("Sản phẩm: " + selectItem.getName() +
                "\nDescription: " + selectItem.getDescription() +
                "\nID người bán: " + selectItem.getSellerId());
        ButtonType yes = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        //gan 2 nut vao thay ok, cancle mac dinh
        confirmAlert.getButtonTypes().setAll(yes, no);
        //cho nguoi dung bam
        ButtonType result = confirmAlert.showAndWait().orElse(null);
        //neu co
        if (result == yes) {
            //tao command gui len server
            DeleteUserCommand cmd = new DeleteUserCommand();
            cmd.addData("itemId", selectItem.getItemId());
            cmd.addData("itemname", selectItem.getName());
            cmd.addData("description", selectItem.getDescription());
            cmd.addData("currentPrice", selectItem.getCurrentPrice());
            cmd.addData("stepPrice", selectItem.getStepPrice());

            connection.sendCommand(cmd);
            Response rp = connection.receiveResponse();

            if (rp.isSuccess()) {
                showAlert("Xóa thành công", rp.getMessage());
                loadItemData();//load lai bang
            } else {
                showAlert("Lỗi", rp.getMessage());
            }

        }
        connection.close();
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
