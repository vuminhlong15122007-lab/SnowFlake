package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;

import java.util.List;

public class GetAllUsersCommand extends Command {
    @Override
    public Response handle() {
        try {
            List<User> users = UserDAO.getInstance().selectAll();
            return new Response(true, "Lấy danh sách user thành công", users);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi server: " + e.getMessage(), null);
        }
    }
}
