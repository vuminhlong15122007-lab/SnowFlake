package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.domain.SellerNotification;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.NotificationDAO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSellerNotificationsCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetSellerNotificationsCommand.class);

    @Override
    public Response handle() {
        try {
            int sellerId = (int) this.getData("sellerId");
            List<SellerNotification> notifications = NotificationDAO.getInstance().findBySellerId(sellerId);
            return new Response(true, "Thành công",  notifications, this);
        } catch (ClassCastException | NullPointerException e) {
            return new Response(false, "Dữ liệu đầu vào không hợp lệ", null, this);
        } catch (Exception e) {
            log.error("Lỗi lấy notification cho seller: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}