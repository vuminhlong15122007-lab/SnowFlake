package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.DAOInterface;
import com.javfxtutorial.hethongdaugia.server.factory.ItemDAOFactory;


public class AddAuctionCommand extends Command{ //Dùng để thêm sản phẩm mới từ form.
    @Override
    public Response handle() {
        Auction auction = (Auction) this.getData("Auction");
        Item item = auction.getItem();
        ItemDAOFactory factory = ItemDAOFactory.getFactory(item.getCategory());
        DAOInterface itemDao = factory.createItemDAO();
        int result2 = itemDao.insert(item);
        if (result2 <= 0) {
            return new Response(false, "Lỗi! Không thể thêm Item vào DB", null, this);
        }

        // Chỉ insert Auction khi Item đã được insert thành công và có itemId hợp lệ
        int result1 = AuctionDAO.getInstance().insert(auction);
        if (result1 > 0) {
            return new Response(true, "Thêm sản phẩm mới thành công", auction, this);
        }
        return new Response(false, "Lỗi!!! Thêm thất bại", null, this);
    }

}
