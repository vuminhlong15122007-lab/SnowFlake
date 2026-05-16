package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;

public class RegisterToAuctionCommand extends Command {
    @Override
    public Response handle() {
        Auction currentAuction = (Auction) this.getData("currentAuction");
        AuctionManager.getInstance().registerToAuction(ClientHandlerContextHolder.get(), currentAuction.getAuctionId());
        if (currentAuction == null)
            return new Response(false, "Auction không hợp lệ", null, this);
        AuctionManager.getInstance().registerToAuction(
                ClientHandlerContextHolder.get(), currentAuction.getAuctionId());
        return new Response(true, "Đăng ký tham gia thành công", null, this);
    }
}
