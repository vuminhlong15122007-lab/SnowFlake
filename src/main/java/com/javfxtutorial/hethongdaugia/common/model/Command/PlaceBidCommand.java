package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;

import java.io.IOException;
import java.util.HashMap;

public class PlaceBidCommand extends Command {
    // trả lại giá giá và bidderName
    @Override
    public Response handle() {
        Auction currentAuction = (Auction) this.getData("currentAuction");
        AuctionManger.getInstance().setCurrentAuction(currentAuction);
        double amount = (double) this.getData("amount");
        if (AuctionManger.getInstance().placeBid(amount)){//nếu đặt giá thành công trả về true
            try {
                ClientHandler.broadcast(new Response(true, "Đặt giá thành công", this.getData(), this));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Response(true, "Đặt giá thành công", this.getData(), this);
        }
        return new Response(false, "Đặt giá không thành công", null);
    }
}
