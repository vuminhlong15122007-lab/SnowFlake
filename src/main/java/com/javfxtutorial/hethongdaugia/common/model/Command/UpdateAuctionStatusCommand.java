package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateAuctionStatusCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(UpdateAuctionStatusCommand.class);
  private Auction auction;

  public UpdateAuctionStatusCommand(Auction auction) {
    this.auction = auction;
  }

  @Override
  public Response handle() throws DataException {
    if (auction == null) {
      return new Response(false, "Du lieu dau vao khong hop le", null, this);
    }
    int result1 = AuctionDAO.getInstance().update(auction);
    if (result1 > 0) {
      AuctionStatus status = auction.getStatus();
      AuctionManager.getInstance().updateAuctionStatus(auction.getAuctionId(), status);

      if (status == AuctionStatus.CANCELLED || status == AuctionStatus.CANCELLED_BY_ADMIN) {
        ClientHandler.broadcast(new Response(false, "ADMIN_CANCELLED_AUCTION", auction, this));
        String productName =
            (auction.getItem() != null)
                ? auction.getItem().getName()
                : String.valueOf(auction.getAuctionId());

        SellerNotification.Type notifType =
            (status == AuctionStatus.CANCELLED_BY_ADMIN)
                ? SellerNotification.Type.CANCELLED_BY_ADMIN
                : SellerNotification.Type.CANCELLED;

        SellerNotification notif =
            new SellerNotification(auction.getAuctionId(), notifType, productName, null, null);

        try {
          ClientHandler.broadcastToSeller(
              auction.getSellerId(), new Response(true, "ADMIN_CANCELLED_AUCTION", notif, this));

        } catch (Exception e) {
          log.error(
              "Lỗi lưu/gửi notification cho seller id={}, auctionId={}: {}",
              auction.getSellerId(),
              auction.getAuctionId(),
              e.getMessage(),
              e);
        }

        ClientHandler.broadcast(new Response(true, "ADMIN_CANCELLED_AUCTION", notif, this));
        return new Response(true, "ADMIN_CANCELLED_AUCTION", notif, this);
      }

      Response rp = new Response(true, "Cập nhật status thành công", auction, this);
      ClientHandler.broadcast(rp);
      return rp;
    }

    return new Response(false, "Cập nhật status thất bại", null, this);
  }
}
