package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataInsertException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddAccountCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(AddAccountCommand.class);

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
        }catch (IllegalArgumentException e) {
            log.warn("AccountType không hợp lệ: {}", type);
            return new Response(false, "Vai trò không hợp lệ", null, this);
        }
        try{
            int result = UserDAO.getInstance().insert(new User(username1, password, email, sdt, role));
            return new Response(true, "tạo tài khoản thành công", null, this);} catch (DuplicateKeyException e) {
            log.warn("Tạo tài khoản thất bại: {}", e.getMessage());
            return new Response(false, e.getMessage(), null, this);
        } catch (DataInsertException e) {
            log.error("Lỗi insert user: {}", e.getMessage(), e);
            return new Response(false, "Không thể tạo tài khoản, vui lòng thử lại", null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi tạo tài khoản: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
