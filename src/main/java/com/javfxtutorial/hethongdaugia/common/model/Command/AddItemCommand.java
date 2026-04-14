package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.ItemManager;

import java.util.ArrayList;

public class AddItemCommand extends Command{ //Dùng để thêm sản phẩm mới từ form.

    @Override
    public Response handle() {
        Item item = (Item) this.getData("Item");
        Auction auction = (Auction) this.getData("Auction");


        if(ItemManager.getInstance().addItem(item) ){
            if (ItemManager.getInstance().addAuction(auction)) {
                return new Response(true, "Thêm thành công", item);
            }
        }
        return new Response(false, "Lỗi!!! Thêm thất bại", null);
    }

}
