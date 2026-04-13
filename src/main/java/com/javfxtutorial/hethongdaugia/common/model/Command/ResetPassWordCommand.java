package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class ResetPassWordCommand extends Command {
    @Override
    public Response handle() {
        int userId = (int) this.getData("userId");
        String passWord = (String) this.getData("passWord");

        boolean resetPW = UserManager.getInstance().reset_password(userId, passWord);
        if(resetPW){
            return new Response(true, "Đổi mật khẩu thành công", null);
        }else{
            return new Response(false, "Lỗi", null);
        }
    }
}
