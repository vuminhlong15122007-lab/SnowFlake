package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionAlreadyEndedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.BidAmountExceedsLimitException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceBidCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(PlaceBidCommand.class);

    @Override
    public Response handle() {
        BidTransaction bid = (BidTransaction) this.getData("bid");
        try {
            if (AuctionManager.getInstance().placeBid(bid, ClientHandlerContextHolder.get())) {
                return new Response(true, "Đặt giá thành công", bid, this);
            }
            return new Response(false, "Cần đặt giá lớn hơn giá hiện tại + bước giá", bid, this);
        } catch (AuctionNotFoundException e) {
            log.warn("Không tìm thấy auction: {}", e.getMessage());
            return new Response(false, "AUCTION_CANCELLED", null, this);
        } catch (AuctionNotStartedException e) {
            log.warn("Auction chưa bắt đầu: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (AuctionAlreadyEndedException e) {
            log.warn("Auction đã kết thúc: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (LowerThanCurrentBidException e) {
            log.warn("Giá thấp hơn giá hiện tại: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (BidAmountExceedsLimitException e) {
            log.warn("Giá vượt giới hạn: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (SelfBidException e) {
            log.warn("Tự đặt giá: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (InsufficientIncrementException e) {
            log.warn("Bước giá không đủ: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (DataException e) {
            log.error("Lỗi database khi đặt giá: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống, vui lòng thử lại", null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi đặt giá: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
