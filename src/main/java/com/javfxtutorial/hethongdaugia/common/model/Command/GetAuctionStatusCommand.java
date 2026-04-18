package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

public class GetAuctionStatusCommand extends Command {

    private Auction auction;

    public GetAuctionStatusCommand(Auction auction) {
        this.auction = auction;
    }

    @Override
    public Response handle() {
        AuctionStatus nowStatus = AuctionManger.getInstance().refreshAuctionStatus(auction);
        return new Response(true, "Lấy trạng thái hiện tại thành công", nowStatus);


    }
}