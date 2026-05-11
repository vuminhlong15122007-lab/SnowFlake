package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.factory.ItemDAOFactory;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;

public class DeleteAuctionCommand extends Command {
    private Auction auction;
    public DeleteAuctionCommand(Auction auction) {this.auction = auction;}

    @Override
    public Response handle() {
        AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
        if (status == AuctionStatus.NOT_START) {
            int result1 = AuctionDAO.getInstance().delete(auction);
            ItemDAOFactory factory = ItemDAOFactory.getFactory(auction.getItem().getCategory());
            DAOInterface<Item> itemDao = factory.createItemDAO();
            int result2 = itemDao.delete(auction.getItem());
            if (result1 > 0 && result2 > 0) { //nghĩa là xóa thành công
                return new Response(true, "Xóa thành công", null, this);
            }
        }
        return new Response(false, "Không thể xóa, phiên đấu giá đang diễn ra hoặc đã kết thúc", null, this);
    }

}