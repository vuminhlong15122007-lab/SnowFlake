package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.NotificationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkNotificationReadCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(MarkNotificationReadCommand.class);

    @Override
    public Response handle() {
        try {
            int auctionId = (int) this.getData("auctionId");
            int updated = NotificationDAO.getInstance().markAsRead(auctionId);
            if (updated > 0) {
                return new Response(true, "Đánh dấu đã đọc thành công", auctionId, this);
            }
            return new Response(false, "Không tìm thấy notification", null, this);
        } catch (ClassCastException | NullPointerException e) {
            return new Response(false, "Dữ liệu đầu vào không hợp lệ", null, this);
        } catch (Exception e) {
            log.error("Lỗi markAsRead notification: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}