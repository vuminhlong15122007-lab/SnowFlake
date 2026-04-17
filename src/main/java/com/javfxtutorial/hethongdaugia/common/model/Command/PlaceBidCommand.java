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
        Auction currentAuction = AuctionManger.getInstance().getCurrentAuction(bid.getAuctionId());
        if (AuctionManger.getInstance().placeBid(currentAuction, bid.getAmount())){//nếu đặt giá thành công trả về true
            try {
                BidDAO.getInstance().insertBid(bid);
                ClientHandler.broadcast(new Response(true, "Đặt giá thành công", bid, this));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Response(true, "Đặt Bid thành công", bid, this);
        }
        return new Response(false, "Cần đặt giá cao hơn giá hiện tại + bước giá", null);
    }
}
