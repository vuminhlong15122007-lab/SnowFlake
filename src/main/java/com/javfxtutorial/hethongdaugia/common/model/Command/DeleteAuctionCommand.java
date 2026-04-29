package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.dao.JDBCUtil;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;
import com.javfxtutorial.hethongdaugia.server.manager.ItemManager;

import java.sql.Connection;
import java.sql.SQLException;

public class DeleteAuctionCommand extends Command {
    private Auction auction;
    public DeleteAuctionCommand(Auction auction) {this.auction = auction;}

    @Override
    public Response handle() {
        AuctionStatus status = AuctionManger.getInstance().refreshAuctionStatus(auction);
        if (status == AuctionStatus.NOT_START) {
            int result1 = AuctionDAO.getInstance().delete(auction);
            int result2 = ItemDAO.getInstance().delete(auction.getItem());
            if (result1 > 0 && result2 > 0) { //nghĩa là xóa thành công
                return new Response(true, "Xóa thành công", null, this);
            }
        }
        return new Response(false, "Không thể xóa, phiên đấu giá đang diễn ra hoặc đã kết thúc", null, this);
    }

}