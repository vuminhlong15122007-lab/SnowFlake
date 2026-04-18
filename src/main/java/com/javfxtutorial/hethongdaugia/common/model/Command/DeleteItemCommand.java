package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.ItemManager;

public class DeleteItemCommand extends Command {
    private int itemId;

    public DeleteItemCommand(int itemId) {this.itemId = itemId;}

    @Override
    public Response handle() {
        // Lấy itemId từ dữ liệu nếu chưa được set qua constructor
        if (itemId == 0) {
            int itemId = (int) this.getData("itemID");
            if (itemId == 0){
                return new Response(false, "Lấy itemId thất bại", null);
            }
        }

        boolean success = ItemManager.getInstance().deleteItem(itemId); // Gọi Manager để xóa
        if (success) {
            return new Response(true, "Xóa sản phẩm thành công", null);
        } else {
            return new Response(false, "Xóa thất bại (có thể sản phẩm không tồn tại hoặc đang có phiên đấu giá)", null);
        }
    }
}