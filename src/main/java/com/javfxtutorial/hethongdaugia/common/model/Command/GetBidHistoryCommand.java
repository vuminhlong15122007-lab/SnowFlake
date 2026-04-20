package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;

import java.util.ArrayList;

public class GetBidHistoryCommand extends Command {
    @Override
    public Response handle() {
        int auctionId = (int) this.getData("auctionId");
        ArrayList<BidTransaction> bidHistory = BidDAO.getInstance().getBidsByAuctionId(auctionId);
        if (bidHistory == null){
            return new Response(false, "Lấy không thành công", null, this);
        }
        return new Response(true, "Lấy thành công", bidHistory, this);
    }
}
