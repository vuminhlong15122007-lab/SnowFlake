package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class AddAccountCommand extends Command {

    @Override
    public Response handle() {
        String username1 = (String) this.getData("username");
        boolean isUsernameExisted = UserManager.getInstance().checkExistedUsername(username1);
        if (isUsernameExisted){
            return new Response(false, "username đã tồn tại", null, this);
        }
        String password = (String) this.getData("password");
        String email = (String) this.getData("email");
        String sdt = (String) this.getData("sdt");
        String type = (String) this.getData("accountType");
        AccountType role = null;
        try{
            role = AccountType.valueOf(type);
        }catch (Exception e){
            e.printStackTrace();
        }
        UserDAO.getInstance().insert(new User(username1, password, email, sdt, role));
        return new Response(true, "tạo tài khoản thành công", null, this);
    }
}
