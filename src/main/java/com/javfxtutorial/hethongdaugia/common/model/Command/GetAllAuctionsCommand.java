package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class GetAllAuctionsCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetAllAuctionsCommand.class);
    @Override
    public Response handle() {
        try{
            ArrayList<Auction> allAuctions = AuctionDAO.getInstance().selectAll();
            for (Auction auction: allAuctions) { // trước khi load lên kiểm tra lại trạng thái của auction
                AuctionStatus status = AuctionManager.getInstance().refreshAuctionStatus(auction);
                auction.setStatus(status);
            }
            return new Response(true, "Lấy thành công", allAuctions, this);} catch (QueryExecutionException e) {
            log.error("Lỗi truy vấn database: {}", e.getMessage(), e);
            return new Response(false, "Lỗi truy vấn dữ liệu", null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
