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

public class UpdateAuctionCommand extends Command {
    private Auction auction;

    public UpdateAuctionCommand(Auction auction) {
        this.auction = auction;
    }
    @Override
    public Response handle() {
        AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
        if (status == AuctionStatus.NOT_START) {
            Item item = auction.getItem();
            ItemDAOFactory factory = ItemDAOFactory.getFactory(item.getCategory());
            DAOInterface itemDao = factory.createItemDAO();
            int result2 = itemDao.update(item);
            int result1 = AuctionDAO.getInstance().update(auction);
            if (result1 > 0 && result2 > 0) {
                return new Response(true, "Sửa sản phẩm  thành công", auction, this);
            }
        }
        return new Response(false, "Lỗi!!! Sửa thất bại", null, this);
    }
}
