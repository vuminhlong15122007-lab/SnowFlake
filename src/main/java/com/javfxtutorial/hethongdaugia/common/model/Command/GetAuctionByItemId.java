package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

public class GetAuctionByItemId extends Command {
    @Override
    public Response handle() {
        int itemId = (int) this.getData("itemId");
        System.out.println("===> Server đang tìm Auction cho Item ID: " + itemId); //oke
        Auction auction =  AuctionDAO.getInstance().selectByItemId(itemId); //lấy auction trong database ra //bị null
        if (auction == null){
            return new Response(false, "itemid không tồn tại", null);
        }
        System.out.println("Đã lấy được auction:" + auction.toString());
        AuctionStatus status = AuctionManger.getInstance().refreshAuctionStatus(auction);
        auction.setStatus(status);
        return new Response(true, "Thành công", auction);
    }
}
