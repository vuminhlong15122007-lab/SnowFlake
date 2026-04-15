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


    public String checkValueProduct(Item item){   // ktra logic truoc khi xu ly
        if (item.getName() == null || item.getName().trim().isEmpty())
            return "Tên sản phẩm không được để trống";
        if (item.getDescription() == null || item.getDescription().trim().isEmpty())
            return "Mô tả không được để trống";
        if (item.getCurrentPrice() <= 0)
            return "Giá khởi điểm phải lớn hơn 0";
        if (item.getStepPrice() <= 0)
            return "Bước giá phải lớn hơn 0";
        if (item.getImagePath() == null || item.getImagePath().trim().isEmpty())
            return "Chưa có ảnh sản phẩm";
        return null;
    }

    public Item addItem (Item item){
        if(checkValueProduct(item) == null){
            int soDong = ItemDAO.getInstance().insert(item); // tao moi 1 ban ghi chua tug co trong DB
            if (soDong > 0){
                return item;  //Trả về ID (do DAO gán)
            }
        }
        return null; // thất bại
    }

    public boolean addAuction (Auction auction ){
        int soDong = AuctionDAO.getInstance().insert(auction); // tao moi 1 ban ghi chua tug co trong DB
        if (soDong > 0){
            return true;
        }
        return false;
    }

    public boolean deleteItem(int itemId) {
        // Tạo đối tượng Item chỉ chứa itemId
        Item item = new Item();
        item.setItemId(itemId);

        // Gọi DAO với đối tượng Item
        int rows = ItemDAO.getInstance().delete(item);
        return rows > 0;
    }


}
