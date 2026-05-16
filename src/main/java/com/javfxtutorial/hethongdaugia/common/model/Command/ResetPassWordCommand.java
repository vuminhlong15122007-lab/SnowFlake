package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetPassWordCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(ResetPassWordCommand.class);
    @Override
    public Response handle() {
        int userId = (int) this.getData("userId");
        String passWord = (String) this.getData("passWord");
        try{
            User resetPW = UserManager.getInstance().reset_password(userId, passWord);
            if(resetPW != null){
                return new Response(true, "Đổi mật khẩu thành công", resetPW, this);
            }else{
                return new Response(false, "Lỗi", null, this);
            }} catch (UserNotFoundException e) {
            log.warn("Không tìm thấy user để reset password: id={}", userId);
            return new Response(false, "Không tìm thấy người dùng", null, this);
        } catch (DataException e) {
            log.error("Lỗi database khi reset password: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
