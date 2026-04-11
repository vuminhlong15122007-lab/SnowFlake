package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

public class PlaceBidCommand extends Command {

    @Override
    public Response handle() {
        Auction currentAuction = (Auction) this.getData("currentAuction");
        AuctionManger.getInstance().setCurrentAuction(currentAuction);

        double amount = (double) this.getData("amount");
        if (AuctionManger.getInstance().placeBid(amount)){
            return new Response(true, "Đặt giá thành công", null);
        }
        return new Response(false, "Đặt giá không thành công", null);
    }
}
