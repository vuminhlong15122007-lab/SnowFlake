package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;

public class UpdateAuctionStatusCommand extends Command {
  private Auction auction;

  public UpdateAuctionStatusCommand(Auction auction) {
    this.auction = auction;
  }

  @Override
  public Response handle() {
    int result1 = AuctionDAO.getInstance().update(auction);
    if (result1 > 0) {
      return new Response(true, "Cập nhật status thành công", auction, this);
    }
    return new Response(false, "Cập nhật status thất bại", null, this);
  }
}
