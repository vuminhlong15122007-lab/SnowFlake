package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAuctionsBySellerIdCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetAuctionsBySellerIdCommand.class);

    @Override
    public Response handle() {

        // Gọi AuctionDAO để lấy danh sách Auction theo sellerId
        try {
            int sellerId = (int) this.getData("sellerId");
            ArrayList<Auction> auctions = AuctionDAO.getInstance().selectBySellerId(sellerId);
            auctions.forEach(
                    auction -> {
                        try {
                            AuctionManager.getInstance().refreshAuctionStatus(auction);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return new Response(true, "Thành công", auctions, this);
        } catch (QueryExecutionException e) {
            log.error("Lỗi truy vấn database: {}", e.getMessage(), e);
            return new Response(false, "Lỗi truy vấn dữ liệu", null, this);
        } catch (ClassCastException | NullPointerException e) {
            return new Response(false, "Dữ liệu đầu vào không hợp lệ", null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
