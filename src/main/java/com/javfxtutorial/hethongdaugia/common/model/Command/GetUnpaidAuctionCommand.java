package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUnpaidAuctionCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetUnpaidAuctionCommand.class);
    private final int userId; // lưu thẳng vào field, không dùng HashMap

    public GetUnpaidAuctionCommand(int userId) {
        this.userId = userId; // constructor nhận userId luôn
    }

    @Override
    public Response handle() {
        try {
            ArrayList<Auction> unpaid = AuctionDAO.getInstance().selectUnpaidByWinnerId(userId);
            log.info("User {} có {} phiên chưa thanh toán", userId, unpaid.size());
            return new Response(true, "OK", unpaid, this);
        } catch (Exception e) {
            log.error("Lỗi GetUnpaidAuction: {}", e.getMessage(), e);
            return new Response(false, e.getMessage(), new ArrayList<>(), this);
        }
    }
}
