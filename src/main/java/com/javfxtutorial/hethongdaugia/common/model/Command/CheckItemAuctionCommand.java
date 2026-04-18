package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;

import java.time.LocalDateTime;

public class CheckItemAuctionCommand extends Command {

    private int itemId;

    public CheckItemAuctionCommand(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public Response handle() {
        if(itemId == 0){
            int itemId = (int) this.getData("itemID");
            if(itemId == 0){
                return new Response(false, "Lấy itemId thất bại", null);
            }
        }
        Auction auction = AuctionDAO.getInstance().selectByItemId(itemId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = auction.getStartingTime();
        LocalDateTime end = auction.getEndingTime();

        String condition;
        if (now.isBefore(start)) {
            condition = "CHUA_BAT_DAU";
        } else if (now.isAfter(end)) {
            condition = "DA_KET_THUC";
        } else {
            condition = "DANG_DIEN_RA";
        }
        
        return new Response(true, condition, auction);



    }
}
