package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;

import java.io.IOException;

public class PlaceBidCommand extends Command {
    // trả lại giá giá và bidderName
    @Override
    public Response handle() {
        BidTransaction bid = (BidTransaction) this.getData("bid");
        Auction currentAuction = (Auction) this.getData("currentAuction");
        if (AuctionManger.getInstance().placeBid(currentAuction, bid)){//nếu đặt giá thành công trả về true
                BidDAO.getInstance().insertBid(bid);
            return new Response(true, "Đặt Bid thành công", bid, this);
        }
        return new Response(false, "Cần đặt giá cao hơn giá hiện tại + bước giá", bid, this);
    }
}
