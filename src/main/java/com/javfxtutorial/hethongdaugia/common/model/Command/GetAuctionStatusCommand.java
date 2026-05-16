package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;

public class GetAuctionStatusCommand extends Command {

    private Auction auction;

    public GetAuctionStatusCommand(Auction auction) {
        this.auction = auction;
    }

    @Override
    public Response handle() {
        try{
            AuctionStatus nowStatus = AuctionManager.getInstance().refreshAuctionStatus(auction);
            return new Response(true, "Lấy trạng thái hiện tại thành công", nowStatus, this);} catch (Exception e) {
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }


    }
}