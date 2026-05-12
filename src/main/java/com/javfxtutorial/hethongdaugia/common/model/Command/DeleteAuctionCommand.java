package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;

public class DeleteAuctionCommand extends Command {
    private Auction auction;
    public DeleteAuctionCommand(Auction auction) {this.auction = auction;}

    @Override
    public Response handle() {
        AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
        if (status == AuctionStatus.NOT_START) {
            int result1 = ItemDAO.getInstance().delete(auction.getItem()); // vì item là cha auction nne xóa item auction tự xóa r
            if (result1 < 0) {
                return new Response(false, "Không thể xóa phiên đấu giá", null, this);
            }
            return new Response(true, "xóa phiên đấu giá thành công", null, this);
        }
        return new Response(false, "Không thể xóa, phiên đấu giá đang diễn ra hoặc đã kết thúc", null, this);
    }

}