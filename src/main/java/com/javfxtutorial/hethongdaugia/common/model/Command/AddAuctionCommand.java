package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataInsertException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddAuctionCommand extends Command{
    private static final Logger log = LoggerFactory.getLogger(AddAuctionCommand.class); //Dùng để thêm sản phẩm mới từ form.
    @Override
    public Response handle() {
        Auction auction = (Auction) this.getData("Auction");
        try{
            AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
            auction.setStatus(status);
            Item item = auction.getItem();
            int result1 = ItemDAO.getInstance().insert(item);
            if (result1 <= 0){
                log.info("Thêm item không thành công vào DB");
                return new Response(false, "Lỗi! Không thể thêm Item vào DB", null, this);
            }
            int result2 = AuctionDAO.getInstance().insert(auction);
            if (result2 <= 0) {
                ItemDAO.getInstance().delete(item); //thêm auction không thành công thì xóa item
                log.info("Thêm auction không thành công vào DB");
            return new Response(false, "Lỗi! Không thể thêm Item vào DB", null, this);
            }
            return new Response(true, "Thêm auction vào DB thành công", auction, this); } catch (DataInsertException e) {
            log.error("Lỗi insert dữ liệu: {}", e.getMessage(), e);
            return new Response(false, "Lỗi lưu dữ liệu: " + e.getMessage(), null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }

}
