package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataUpdateException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateAuctionStatusCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(UpdateAuctionStatusCommand.class);
  private Auction auction;

  public UpdateAuctionStatusCommand(Auction auction) {
    this.auction = auction;
  }

  @Override
  public Response handle() {
    try{
      int result1 = AuctionDAO.getInstance().update(auction);
      if (result1 > 0) {
        return new Response(true, "Cập nhật status thành công", auction, this);
      }
      return new Response(false, "Cập nhật status thất bại", null, this); } catch (DataUpdateException e) {
      log.error("Lỗi cập nhật trạng thái: {}", e.getMessage(), e);
      return new Response(false, "Lỗi cập nhật: " + e.getMessage(), null, this);
    } catch (DataException e) {
      log.error("Lỗi dữ liệu khi cập nhật trạng thái: {}", e.getMessage(), e);
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    } catch (Exception e) {
      log.error("Lỗi không xác định khi cập nhật trạng thái: {}", e.getMessage(), e);
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    }
  }
}
