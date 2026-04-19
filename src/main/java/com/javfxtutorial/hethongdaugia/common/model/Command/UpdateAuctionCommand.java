package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;

public class UpdateAuctionCommand extends Command {
    private Auction auction;

    public UpdateAuctionCommand(Auction auction) {
        this.auction = auction;
    }
    public UpdateAuctionCommand() {}

    @Override
    public Response handle() {
        Item item = auction.getItem();
        int result2 = ItemDAO.getInstance().update(item); //sau khi insert item, DAO sẽ tự ộng gắn lại id cho item -> gắn lại iditem cho auction
        auction.getItem().setItemId(item.getItemId());
        int result1 = AuctionDAO.getInstance().update(auction);
        if (result1 > 0 && result2 > 0){
            return new Response(true, "Sửa sản phẩm  thành công", auction);
        }
        return new Response(false, "Lỗi!!! Sửa thất bại", null);


    }
}
