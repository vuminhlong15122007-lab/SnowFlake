package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.ItemManager;

import java.util.ArrayList;

public class AddAuctionCommand extends Command{ //Dùng để thêm sản phẩm mới từ form.
    @Override
    public Response handle() {
        Auction auction = (Auction) this.getData("Auction");
        Item item = auction.getItem();
        int result2 = ItemDAO.getInstance().insert(item); //sau khi insert item, DAO sẽ tự ộng gắn lại id cho item -> gắn lại iditem cho auction
        auction.getItem().setItemId(item.getItemId());
        int result1 = AuctionDAO.getInstance().insert(auction);
        if (result1 > 0 && result2 > 0){
            return new Response(true, "Thêm sản phẩm mới thành công", auction);
        }
        return new Response(false, "Lỗi!!! Thêm thất bại", null);
    }

}
