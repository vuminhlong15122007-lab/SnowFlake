package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;

public class ItemManager {
    private static ItemManager instance;

    private ItemManager(){};

    public static ItemManager getInstance(){
        if (instance == null){
            instance = new ItemManager();
        }
        return instance;

    }

    Item currentItem;


    public boolean checkValueProduct(Item item){   // ktra logic truoc khi xu ly
        if(item.getName() == null){
            return false;
        } else if (item.getCurrentPrice() <= 0 ) {
            return false;
        } else if (item.getDescription() == null) {
            return false;
        }else if(item.getImagePath() == null) {
            return false;
        } else if (item.getStepPrice() <= 0) {
            return false;
        }
        return true;
    }

    public boolean addItem (Item item){
        if(checkValueProduct(item)){
            int soDong = ItemDAO.getInstance().insert(item); // tao moi 1 ban ghi chua tug co trong DB
            if (soDong > 0){
                return true;
            }
        }
        return false;
    }

    public boolean addAuction (Auction auction ){
        int soDong = AuctionDAO.getInstance().insert(auction); // tao moi 1 ban ghi chua tug co trong DB
        if (soDong > 0){
            return true;
        }
        return false;
    }


}
