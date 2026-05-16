package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUserCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserCommand.class);

    @Override
    public Response handle() {
        int userId = (int) this.getData("userId");
        String username = (String) this.getData("username");
        String email = (String) this.getData("email");
        String phone = (String) this.getData("phone");
        try{
            boolean success = UserManager.getInstance().deleteUser(userId, username, email, phone);

            if(success){
                return new Response(true, "Xóa user thành công", null, this);
            }
            return new Response(false, "Xóa thất bại", null, this); } catch (UserNotFoundException e) {
            log.warn("Không tìm thấy user để xóa: id={}", userId);
            return new Response(false, "Không tìm thấy người dùng", null, this);
        } catch (DataException e) {
            log.error("Lỗi database khi xóa user: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
