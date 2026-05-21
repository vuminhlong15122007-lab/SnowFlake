package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.domain.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoBidCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(AutoBidCommand.class);

    @Override
    public Response handle() {
        AutoBidConfig config = (AutoBidConfig) this.getData("autoBidConfig");
        if (config == null) {
            return new Response(false, "Cấu hình không hợp lệ", null, this);
        }
        try {
            boolean success = AuctionManager.getInstance().registerAutoBid(config);
            String message;
            if (!success) {
                message = "Lỗi kích hoạt Bot";
            } else if (config.isActive()) {
                message = "đã bật Bot thành công";
            } else {
                message = "đã tắt Bot thành công";
            }

            return new Response(success, message, config, this);
        } catch (Exception e) {
            log.error("Lỗi AutoBid: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
