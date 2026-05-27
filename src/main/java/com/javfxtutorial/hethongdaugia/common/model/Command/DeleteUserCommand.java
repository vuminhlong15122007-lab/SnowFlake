package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUserCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(DeleteUserCommand.class);

  @Override
  public Response handle() {
    try {
      int userId = (int) this.getData("userId");
      String username = (String) this.getData("username");
      String email = (String) this.getData("email");
      String phone = (String) this.getData("phone");

      boolean success = UserManager.getInstance().deleteUser(userId, username, email, phone);
      if (success) {
        Response rp = new Response(true, "Xóa user thành công", userId, this);
        ClientHandler.broadcastToUserId(rp, userId);
        return rp;
      }
      return new Response(false, "Xóa thất bại", null, this);
    } catch (ClassCastException | NullPointerException e) {
      return new Response(false, "Dữ liệu đầu vào không hợp lệ", null, this);
    } catch (UserNotFoundException e) {
      return new Response(false, "Không tìm thấy người dùng", null, this);
    } catch (DataException e) {
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    } catch (Exception e) {
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    }
  }
}
