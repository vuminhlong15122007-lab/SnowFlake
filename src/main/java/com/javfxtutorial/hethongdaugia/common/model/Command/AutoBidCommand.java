package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

public class AutoBidCommand extends Command {
    @Override
    public Response handle() {
        AutoBidConfig config = (AutoBidConfig) this.getData("autoBidConfig");
        if (config == null) {
            return new Response(false, "Cấu hình không hợp lệ", null, this);
        }

        boolean success = AuctionManger.getInstance().registerAutoBid(config);
        String message;
        if (!success) {
            message = "Lỗi kích hoạt Bot";
        } else if (config.isActive()) {
            message = "đã bật Bot thành công";
        } else {
            message = "đã tắt Bot thành công";
        }

        return new Response(success, message, config, this);
    }
}
