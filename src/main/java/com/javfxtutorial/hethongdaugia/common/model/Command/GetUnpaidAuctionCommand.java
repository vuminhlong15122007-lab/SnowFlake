package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUnpaidAuctionCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(GetUnpaidAuctionCommand.class);
  private final String userName; // lưu thẳng vào field, không dùng HashMap

  public GetUnpaidAuctionCommand(String userName) {
    this.userName = userName; // constructor nhận userId luôn
  }

  @Override
  public Response handle() {
    try {
      ArrayList<Auction> unpaid = AuctionDAO.getInstance().selectUnpaidByWinnerName(userName);
      unpaid.forEach(
          auction -> {
            try {
              AuctionManager.getInstance().refreshAuctionStatus(auction);
            } catch (DataException e) {
              throw new RuntimeException(e);
            }
          });
      log.info("User {} có {} phiên chưa thanh toán", userName, unpaid.size());
      return new Response(true, "OK", unpaid, this);
    } catch (Exception e) {
      log.error("Lỗi GetUnpaidAuction: {}", e.getMessage(), e);
      return new Response(false, e.getMessage(), new ArrayList<>(), this);
    }
  }
}
