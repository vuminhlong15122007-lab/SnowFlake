package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;

import java.util.ArrayList;

public class GetAuctionsBySellerIdCommand extends Command {
    @Override
    public Response handle() {
        int sellerId = (int) this.getData("sellerId");
        // Gọi AuctionDAO để lấy danh sách Auction theo sellerId
        ArrayList<Auction> auctions = AuctionDAO.getInstance().selectBySellerId(sellerId);
        return new Response(true, "Thành công", auctions, this);
    }
}
