package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class UpdateProfileCommand extends Command {
    @Override
    public Response handle() {
        try {
            int userId = (int) this.getData("userId");
            String name = (String) this.getData("username");
            String email = (String) this.getData("email");
            String phone = (String) this.getData("phone");
            String avatar = (String) this.getData("avt");

            User updateUser = UserManager.getInstance().updateUserProfile(userId, name, email, phone, avatar);
            if (updateUser != null) {
                return new Response(true, "Cap nhat thanh cong", updateUser, this);
            }
            return new Response(false, "Cap nhat that bai", null, this);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Khong the cap nhat thong tin.", null, this);
        }
    }
}
