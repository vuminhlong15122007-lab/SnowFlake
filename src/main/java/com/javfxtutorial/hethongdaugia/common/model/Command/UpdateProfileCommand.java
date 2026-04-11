package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class UpdateProfileCommand extends Command {
    @Override
    public Response handle(){
        int userId = (int) this.getData("userId");
        String name = (String) this.getData("username");
        String email = (String) this.getData("email");
        String phone = (String) this.getData("phone");

        //goi sever cap nhat
        User updateUser = UserManager.getInstance().updateUserProfile(userId, name, email, phone);

        if(updateUser != null){
            return new Response(true, "Cập nhật thành công", updateUser);
        }else{
            return new Response(false, "Cập nhật thất bại", null);
        }
    }
}
