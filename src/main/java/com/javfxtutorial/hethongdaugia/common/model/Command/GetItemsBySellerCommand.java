package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;

import java.util.ArrayList;

public class GetItemsBySellerCommand extends Command {
    private int sellerId;

    public GetItemsBySellerCommand(int sellerId) {
        this.sellerId = sellerId;
    }

    @Override
    public Response handle() {
        // Gọi ItemDAO để lấy danh sách Item theo sellerId
        ArrayList<Item> items = ItemDAO.getInstance().selectBySellerId(sellerId);
        return new Response(true, "Thành công", items);
    }
}
