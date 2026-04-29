package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class LoginCommand extends Command {
    @Override
    public Response handle() {
        String username = (String) this.getData("username");
        String password = (String) this.getData("password");
        User user = UserManager.getInstance().authenticate(username, password);
        if (user != null) {
            return new Response(true, "Đăng nhập thành công", user, this);
        }
        return new Response(false, "Sai tên hoặc mật khẩu", null, this);
    }
}
