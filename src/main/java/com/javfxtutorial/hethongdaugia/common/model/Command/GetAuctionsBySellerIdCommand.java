package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class GetAuctionsBySellerIdCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetAuctionsBySellerIdCommand.class);
    @Override
    public Response handle() {
        int sellerId = (int) this.getData("sellerId");
        // Gọi AuctionDAO để lấy danh sách Auction theo sellerId
        try{
            ArrayList<Auction> auctions = AuctionDAO.getInstance().selectBySellerId(sellerId);
            return new Response(true, "Thành công", auctions, this);} catch (QueryExecutionException e) {
            log.error("Lỗi truy vấn database: {}", e.getMessage(), e);
            return new Response(false, "Lỗi truy vấn dữ liệu", null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
