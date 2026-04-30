package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;

public class PlaceBidCommand extends Command {
    @Override
    public Response handle() {
        BidTransaction bid = (BidTransaction) this.getData("bid");
        if (AuctionManger.getInstance().placeBid(bid, ClientHandlerContextHolder.get())) {
            return new Response(true, "Đặt giá thành công", bid, this);
        }
        return new Response(false, "Cần đặt giá lớn hơn giá hiện tại + bước giá", bid, this);
    }
}