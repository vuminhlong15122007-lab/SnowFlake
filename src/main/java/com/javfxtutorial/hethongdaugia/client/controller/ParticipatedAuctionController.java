package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.ImageHelper;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class ParticipatedAuctionController implements Initializable, ResponseListener {
  @FXML
  private Button btnAll;
  @FXML
  private Button btnCTToan;
  @FXML
  private Button btnDTGia;
  @FXML
  private Button btnDTToan;
  @FXML
  private Button goMenu;
  @FXML
  private ListView<Auction> productList;
  @FXML
  private Label sectionTitle;


  private ObservableList<Auction> participatedAuctionList = FXCollections.observableArrayList();

  @FXML
  void goMenu(ActionEvent event) {

  }
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    productList.setItems(participatedAuctionList);
    productList.setCellFactory((ListView<Auction> listview) -> new ParticipatedAuctionCell());
  }

  private void getParticipatedAuctionList(){

  }

  @Override
  public void onResponse(Response rp) {

  }


}
