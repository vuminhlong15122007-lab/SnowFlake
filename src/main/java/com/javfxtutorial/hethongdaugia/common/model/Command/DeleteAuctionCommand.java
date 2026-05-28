package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataDeleteException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAuctionCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(DeleteAuctionCommand.class);
  private final Auction auction;

  public DeleteAuctionCommand(Auction auction) {
    this.auction = auction;
  }

  public Auction getAuction() {
    return auction;
  }

  @Override
  public Response handle() {
    try {
      AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
      if (status == AuctionStatus.NOT_START) {
        int result1 = ItemDAO.getInstance().delete(auction.getItem());
        if (result1 <= 0) {
          return new Response(false, "Không thể xóa phiên đấu giá", null, this);
        }

        Response rp = new Response(true, "xóa phiên đấu giá thành công", null, this);
        ClientHandler.broadcast(rp);
        return rp;
      } else if (status == AuctionStatus.RUNNING) {
        return new Response(false, "Không thể xóa phiên đấu giá đang diễn ra", null, this);
      } else {
        return new Response(false, "Không thể xóa phiên đấu giá đã kết thúc", null, this);
      }

    } catch (DataDeleteException e) {
      log.error("Lỗi xóa dữ liệu: {}", e.getMessage(), e);
      return new Response(false, "Lỗi khi xóa: " + e.getMessage(), null, this);
    } catch (Exception e) {
      log.error("Lỗi không xác định: {}", e.getMessage(), e);
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    }
  }
}
