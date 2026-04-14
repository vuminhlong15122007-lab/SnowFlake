package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;

import java.sql.SQLException;
import java.util.ArrayList;

public class GetAllItemsCommand extends Command {
    @Override
    public Response handle() {   //Dùng để hiển thị danh sách sản phẩm hiện có.
            ArrayList<Item> items;
            items = ItemDAO.getInstance().selectAll();
            return new Response(true, "Lấy thành công", items);

    }

}
