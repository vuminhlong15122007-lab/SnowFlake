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
}
