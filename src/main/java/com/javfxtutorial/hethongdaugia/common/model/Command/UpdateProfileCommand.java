package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateProfileCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(UpdateProfileCommand.class);

    @Override
    public Response handle() {
        try {
            int userId = (int) this.getData("userId");
            String name = (String) this.getData("username");
            String email = (String) this.getData("email");
            String phone = (String) this.getData("phone");
            String avatar = (String) this.getData("avt");

            User updateUser =
                    UserManager.getInstance().updateUserProfile(userId, name, email, phone, avatar);
            if (updateUser != null) {
                return new Response(true, "Cap nhat thanh cong", updateUser, this);
            }
            return new Response(false, "Cap nhat that bai", null, this);
        } catch (UserNotFoundException e) {
            log.warn("Không tìm thấy user để cập nhật: {}", e.getMessage());
            return new Response(false, "Không tìm thấy người dùng", null, this);
        } catch (ClassCastException e) {
            log.error("Lỗi ép kiểu dữ liệu: {}", e.getMessage(), e);
            return new Response(false, "Dữ liệu đầu vào không hợp lệ", null, this);
        } catch (DataException e) {
            log.error("Lỗi database khi cập nhật profile: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi cập nhật profile: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
