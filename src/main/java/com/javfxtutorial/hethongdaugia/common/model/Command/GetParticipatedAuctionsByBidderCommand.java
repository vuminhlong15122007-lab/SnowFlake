package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ParticipatedAuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class GetParticipatedAuctionsByBidderCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(GetParticipatedAuctionsByBidderCommand.class);
  @Override
  public Response handle() {
    int userId = (int) this.getData("currentUserId");
    try{
      ArrayList<Auction> auctions = new ArrayList<>();
      auctions = (ArrayList<Auction>) AuctionManager.getInstance().getParticipatedAuctionsByBidder(userId);
      auctions.forEach(auction -> {
          try {
              auction.setStatus(AuctionManager.getInstance().checkPaymentStatus(auction));
          } catch (DataException e) {
            log.warn("Lỗi khi kiểm tra trạng thái thanh toán cho auction {}: {}",
                    auction.getAuctionId(), e.getMessage());
          }
      });
      return new Response(true, "Lấy thành công auctions của userId: " + userId, auctions, this);} catch (Exception e) {
      log.error("Lỗi không xác định: {}", e.getMessage(), e);
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    }
  }
}
