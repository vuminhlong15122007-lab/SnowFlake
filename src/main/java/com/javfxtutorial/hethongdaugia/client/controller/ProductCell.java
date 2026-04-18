package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import java.io.IOException;

public class ProductCell extends ListCell<Auction> {
    @Override
    protected void updateItem(Auction auction, boolean empty){  // method cua class cha da la protected
        super.updateItem(auction,empty);
        if(empty||auction == null){  // Tuc la man hinh da khong hien thi sp nua . tinh nang cua ListView
            setText(null); // lam null chu de tai sd lai cai itemphien... y
            setGraphic(null);
            }else{
                try{
                    // Nap man hinh giao dien itemphien..
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javfxtutorial/hethongdaugia/view/itemphiendaugia.fxml"));
                    Parent root = loader.load(); // tim FXML de doc giao dien va tao giao dien xac

                // Truyen vao controller
                    AuctionSessionController controller = loader.getController();
                    controller.setData(auction); // truyen vao du lieu cho Itemphien...

                    setGraphic(root); // hien thi giao dien xac da tao ra man hinh

            }catch (IOException e){
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
        }
    }

}
