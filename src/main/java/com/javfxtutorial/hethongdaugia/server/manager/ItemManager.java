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



    public boolean deleteItem(int itemId) {
        // Tạo đối tượng Item chỉ chứa itemId
        Item item = new Item();
        item.setItemId(itemId);

        // Gọi DAO với đối tượng Item
        int rows = ItemDAO.getInstance().delete(item);
        return rows > 0;
    }

}
