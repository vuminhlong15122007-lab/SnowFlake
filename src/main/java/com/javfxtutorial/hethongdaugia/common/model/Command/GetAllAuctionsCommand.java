package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

import java.util.ArrayList;

public class GetAllAuctionsCommand extends Command {
    @Override
    public Response handle() {
        ArrayList<Auction> allAuctions = AuctionDAO.getInstance().selectAll();
        for (Auction auction: allAuctions) { // trước khi load lên kiểm tra lại trạng thái của auction
            AuctionStatus status = AuctionManger.getInstance().refreshAuctionStatus(auction);
            auction.setStatus(status);
        }
        return new Response(true, "Lấy thành công", allAuctions);
    }
}
