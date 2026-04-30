package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;

public class RegisterToAuctionCommand extends Command {
    @Override
    public Response handle() {
        Auction currentAuction = (Auction) this.getData("currentAuction");
        AuctionManger.getInstance().registerToAuction(ClientHandlerContextHolder.get(), currentAuction.getAuctionId());
        return null;
    }
}
