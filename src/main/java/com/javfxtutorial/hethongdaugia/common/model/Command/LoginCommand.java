package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginCommand extends Command {
  private static final Logger log = LoggerFactory.getLogger(LoginCommand.class);

  @Override
  public Response handle() {
    String username = (String) this.getData("username");
    String password = (String) this.getData("password");
    try {
      User user = UserManager.getInstance().authenticate(username, password);
      if (user != null) {
        ClientHandler currentClient = ClientHandlerContextHolder.get();
        if (currentClient != null) {
          currentClient.setCurrentUser(user);
        }
        return new Response(true, "Đăng nhập thành công", sanitize(user), this);
      }
      return new Response(false, "Sai tên hoặc mật khẩu", null, this);
    } catch (InvalidCredentialsException e) {
      log.warn("Đăng nhập thất bại: {}", username);
      return new Response(false, "Sai tên đăng nhập hoặc mật khẩu", null, this);
    } catch (DataException e) {
      log.error("Lỗi database khi đăng nhập: {}", e.getMessage(), e);
      return new Response(false, "Lỗi hệ thống, vui lòng thử lại sau", null, this);
    } catch (Exception e) {
      log.error("Lỗi không xác định: {}", e.getMessage(), e);
      return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
    }
  }

  private static User sanitize(User user) {
    return new User(
        user.getId(),
        user.getName(),
        null,
        user.getEmail(),
        user.getSdt(),
        user.getAccountType(),
        user.getImagePath());
  }
}
