package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class DeleteUserCommand extends Command {
    @Override
    public Response handle() {
        int userId = (int) this.getData("userId");
        String username = (String) this.getData("username");
        String email = (String) this.getData("email");
        String phone = (String) this.getData("phone");

        boolean success = UserManager.getInstance().deleteUser(userId, username, email, phone);

        if(success){
            return new Response(true, "Xóa user thành công", null, this);
        }
        return new Response(false, "Xóa thất bại", null, this);
    }
}
