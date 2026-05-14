package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class RegisterCommand extends Command {
    @Override
    public Response handle() {
        String username1 = (String) this.getData("username");
        String password = (String) this.getData("password");
        String email = (String) this.getData("email");
        String sdt = (String) this.getData("sdt");
        int result = UserDAO.getInstance().insert(new User(username1, password, email, sdt, AccountType.USER));
        if (result == -1){
            return new Response(false, "Username đã tồn tại, vui lòng đặt username khác", null, this);
        } else if (result == 0){
            return new Response(false, "tạo tài khoản thất bại", null, this);
        }
        return new Response(true, "tạo tài khoản thành công", null, this);
    }
}
