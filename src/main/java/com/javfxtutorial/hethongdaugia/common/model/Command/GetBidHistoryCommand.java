package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetBidHistoryCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetBidHistoryCommand.class);

    @Override
    public Response handle() {
        try {
            int auctionId = (int) this.getData("auctionId");
            ArrayList<BidTransaction> bidHistory =
                    BidDAO.getInstance().getBidsByAuctionId(auctionId);
            if (bidHistory == null) {
                return new Response(false, "Lấy không thành công", null, this);
            }
            return new Response(true, "Lấy thành công", bidHistory, this);
        } catch (ClassCastException e) {
            log.error("Lỗi ép kiểu dữ liệu: {}", e.getMessage(), e);
            return new Response(false, "Dữ liệu đầu vào không hợp lệ", null, this);
        } catch (QueryExecutionException e) {
            log.error("Lỗi truy vấn database: {}", e.getMessage(), e);
            return new Response(false, "Lỗi truy vấn dữ liệu: " + e.getMessage(), null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
