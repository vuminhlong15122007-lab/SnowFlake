package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.EntityNotFoundException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateAuctionCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(UpdateAuctionCommand.class);
    private Auction auction;

    public UpdateAuctionCommand(Auction auction) {
        this.auction = auction;
    }

    @Override
    public Response handle() {
        try {
            AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
            if (status == AuctionStatus.NOT_START) {
                Item item = auction.getItem();
                int result2 = ItemDAO.getInstance().update(item);
                int result1 = AuctionDAO.getInstance().update(auction);
                if (result1 > 0 || result2 > 0) {
                    Response rp = new Response(true, "Sửa sản phẩm  thành công", auction, this);
                    ClientHandler.broadcast(rp);
                    return rp;
                }
                return new Response(false, "Lỗi!!! Sửa thất bại", null, this);
            }
            return new Response(
                    false,
                    "Lỗi!!! Sửa thất bại, phiên đấu giá đã bắt đầu hoặc kết thúc",
                    null,
                    this);
        } catch (EntityNotFoundException e) {
            log.error("Không tìm thấy auction: auctionId={}", auction.getAuctionId(), e);
            return new Response(false, "Không tìm thấy phiên đấu giá", null, this);
        } catch (DataException e) {
            log.error("Lỗi dữ liệu khi cập nhật auction: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi cập nhật auction: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
