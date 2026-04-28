package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

public class PlaceBidCommand extends Command {
    @Override
    public Response handle() {
        BidTransaction bid = (BidTransaction) this.getData("bid");
        Auction currentAuction = (Auction) this.getData("currentAuction");

        if (AuctionManger.getInstance().placeBid(currentAuction, bid)) {
            return null;
        }

        return new Response(false, "Cần đặt giá cao hơn giá hiện tại + bước giá", bid, this);
    }
}