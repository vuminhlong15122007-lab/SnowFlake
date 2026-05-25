package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataUpdateException;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.NotificationDAO;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
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
        try {
            if (auction == null) {
                return new Response(false, "Du lieu dau vao khong hop le", null, this);
            }
            int result1 = AuctionDAO.getInstance().update(auction);
            if (result1 > 0) {
                AuctionStatus status = auction.getStatus();

                // Cập nhật RAM trong AuctionManager cho mọi status
                AuctionManager.getInstance().updateAuctionStatus(auction.getAuctionId(), status);

                // Gửi notification về seller tương ứng với từng loại hủy
                if (status == AuctionStatus.CANCELLED || status == AuctionStatus.CANCELLED_BY_ADMIN) {
                    String productName = (auction.getItem() != null) ? auction.getItem().getName() : String.valueOf(auction.getAuctionId());

                    SellerNotification.Type notifType = (status == AuctionStatus.CANCELLED_BY_ADMIN) ? SellerNotification.Type.CANCELLED_BY_ADMIN : SellerNotification.Type.CANCELLED;

                    SellerNotification notif = new SellerNotification(
                            auction.getAuctionId(),
                            notifType,
                            productName,
                            null,
                            null
                    );
                    // Lưu vào DB để seller load lại sau khi tắt/mở app
                    NotificationDAO.getInstance().insertOrReplace(notif, auction.getSellerId());
                    Response notifResponse = new Response(true, "ADMIN_CANCELLED_AUCTION", notif, this);
                    ClientHandler.broadcastToSeller(auction.getSellerId(), notifResponse);
                    ClientHandler.broadcast(
                            new Response(false, "ADMIN_CANCELLED_AUCTION", auction, this)
                    );
                }
                Response rp = new Response(true, "Cập nhật status thành công", auction, this);
                ClientHandler.broadcast(rp);
                return rp;
            }
            return new Response(false, "Cập nhật status thất bại", null, this);
        } catch (DataUpdateException e) {
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