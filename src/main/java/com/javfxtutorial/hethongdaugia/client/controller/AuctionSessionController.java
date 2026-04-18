package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAuctionByItemId;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.sun.source.tree.TryTree;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import com.javfxtutorial.hethongdaugia.common.model.Item;

public class AuctionSessionController {
    @FXML private Label AuctionStatusText;
    @FXML private Label ItemNameLabel;
    @FXML private Label ItemPriceText;
    @FXML private Label SellerNameText;
    @FXML private Label StartTimeText;
    @FXML private ImageView imgSanPham;

    private Item item ;
    private Auction auction;

    //lấy item từ db và load lên màn hình
    public void setData(Item item) throws IOException, ClassNotFoundException {   // xu ly du lieu tu Obj den giao dien
        if (item == null || item.getItemId() <= 0) {
            return;
        }
        this.item = item;
        ItemNameLabel.setText(item.getName());
        SellerNameText.setText(item.getSellerName());

        //tìm auction theo itemid ược truyền vào = get auctionbyitemid
        ServerConnection connection = new ServerConnection();
        Command cmd = new GetAuctionByItemId();
        cmd.addData("itemId", item.getItemId());
        connection.sendCommand(cmd);
        Response rp = connection.receiveResponse();
        connection.close();
        if (rp.isSuccess()) {
            auction = (Auction) rp.getPayLoad();
            System.out.println(auction.toString());
            StartTimeText.setText(String.valueOf(auction.getStartingTime()));
            ItemPriceText.setText(String.valueOf(auction.getInitPrice()));
            AuctionStatusText.setText(String.valueOf(auction.getStatus()));
        }else {
            System.out.println(rp.getMessage());
        }

        }

    @FXML
    public void btnLiveAuction(ActionEvent event){
            try {
                ClientModel.getInstance().setCurrentItem(item);
                ClientModel.getInstance().setCurrentAuction(auction);
                System.out.println("Sản phẩm đang ấn vào có id là: " + ClientModel.getInstance().getCurrentItem().getItemId());
                System.out.println("Auction đang ấn vào là: " + ClientModel.getInstance().getCurrentAuction());
                // Nap man hinh giao dien man_hinh_sp
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/man_hinh_hien_thi_sp.fxml"));
                Parent root = loader.load();  // tim file FXML doc ban ve va tao giao dien xac ( chua co bo nao)

                // Lech chuyen man
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);  //Chuyen man = setRoot

            } catch (IOException e) {
                e.printStackTrace();

            }

    }

}
