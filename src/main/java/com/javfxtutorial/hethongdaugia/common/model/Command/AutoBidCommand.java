package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManger;

public class AutoBidCommand extends Command {
    public Response handle(){
        AutoBidConfig config = (AutoBidConfig) this.getData("autoBidConfig");
        if (config !=  null){
            boolean success = AuctionManger.getInstance().registerAutoBid(config);
            return new Response(success, success ? "Đã bật Bot thành công" : "Lỗi kích hoạt Bot", config, this);
        }
        return new Response(false, "Cấu hình không hợp lệ", null, this);
    }
}
