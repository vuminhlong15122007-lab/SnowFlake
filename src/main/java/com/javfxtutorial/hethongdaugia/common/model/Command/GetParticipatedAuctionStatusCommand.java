package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ParticipatedAuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import javafx.application.Application;
import javafx.stage.Stage;
import kotlin.contracts.Returns;

public class GetParticipatedAuctionStatusCommand extends Command {

    private Auction auction;

    public GetParticipatedAuctionStatusCommand(Auction auction) {
        this.auction = auction;
    }

    @Override
    public Response handle() {
//        ParticipatedAuctionStatus nowStatus =   CHUA VIET DAO
//        return new Response(true, "Lấy trạng thái  thành công", nowStatus, this);

        return null;
    }
}