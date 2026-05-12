package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ParticipatedAuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;

import java.util.ArrayList;

public class GetParticipatedAuctionsByBidderCommand extends Command {
  @Override
  public Response handle() {
    int userId = (int) this.getData("currentUserId");
    ArrayList<Auction> auctions = new ArrayList<>();
    auctions = (ArrayList<Auction>) AuctionManager.getInstance().getParticipatedAuctionsByBidder(userId);
    auctions.forEach(auction -> auction.setStatus(AuctionManager.getInstance().checkPaymentStatus(auction)));
    return new Response(true, "Lấy thành công auctions của userId: " + userId, auctions, this);
  }
}
