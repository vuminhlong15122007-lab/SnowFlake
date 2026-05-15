package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

public class AddAccountCommand extends Command {
    private final int NOT_UNIQUE_USERNAME = -1;
    private final int NOT_UNIQUE_SDT = -2;
    private final int NOT_UNIQUE_EMAIL = -3;
    @Override
    public Response handle() {
        String username1 = (String) this.getData("username");
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
        int result = UserDAO.getInstance().insert(new User(username1, password, email, sdt, role));
        if (result == NOT_UNIQUE_USERNAME){
            return new Response(false, "Username đã tồn tại, vui lòng đặt username khác", null, this);
        }
        else if (result == NOT_UNIQUE_EMAIL){
            return new Response(false, "Email đã tồn tại, vui lòng đặt email khác", null, this);
        }
        else if (result == NOT_UNIQUE_SDT){
            return new Response(false, "Số điện thoại đã tồn tại, vui lòng đặt SĐT khác", null, this);
        }else if (result == 0){
            return new Response(false, "tạo tài khoản thất bại", null, this);
        }
        return new Response(true, "tạo tài khoản thành công", null, this);
    }
}
