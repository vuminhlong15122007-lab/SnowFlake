package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.EntityNotFoundException;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManager {
  private static final Logger log = LoggerFactory.getLogger(UserManager.class);

  private static volatile UserManager instance;

  private UserManager() {}

  public static UserManager getInstance() {
    if (instance == null) {
      synchronized (UserManager.class) {
        if (instance == null) {
          instance = new UserManager();
        }
      }
    }
    return instance;
  }

  public User authenticate(String username, String password)
      throws InvalidCredentialsException, DataException {
    try {
      User user = UserDAO.getInstance().selectByUsername(username);
      if (user == null) {
        log.warn("Không tìm thấy user: {}", username);
        throw new InvalidCredentialsException();
      }
      if (!PasswordHasher.matches(password, user.getPassWord())) {
        log.warn("Mật khẩu sai cho user: {}", username);
        throw new InvalidCredentialsException();
      }

      if (!PasswordHasher.isHashed(user.getPassWord())) {
        user.setPassWord(PasswordHasher.hash(password));
        UserDAO.getInstance().update(user);
      }
      log.info("Xác thực thành công: {}", username);
      return user;
    } catch (EntityNotFoundException e) {
      log.warn("Không tìm thấy user: {}", username);
      throw new InvalidCredentialsException();
    }
  }

  public User updateUserProfile(int userId, String username, String email, String phone, String avt)
      throws UserNotFoundException, DataException {
    log.info("Cập nhật profile: userId={}", userId);
    try {
      User oldUser = UserDAO.getInstance().selectById(userId);
      if (oldUser == null) {
        return null;
      }

      String resolvedName = mergeText(oldUser.getName(), username);
      String resolvedEmail = mergeText(oldUser.getEmail(), email);
      String resolvedPhone = mergeText(oldUser.getSdt(), phone);
      String resolvedAvatar = avt == null ? oldUser.getImagePath() : avt;

      User updateUser =
          new User(
              userId,
              resolvedName,
              oldUser.getPassWord(),
              resolvedEmail,
              resolvedPhone,
              oldUser.getAccountType(),
              resolvedAvatar);

      int result = UserDAO.getInstance().update(updateUser);
      if (result > 0) {
        return updateUser;
      }
      return null;
    } catch (EntityNotFoundException e) {
      throw new UserNotFoundException(userId);
    }
  }

  public boolean deleteUser(int userId, String username, String email, String phone)
      throws UserNotFoundException, DataException {
    try {
      User deleteUser = UserDAO.getInstance().selectById(userId);
      if (deleteUser == null) {
        return false;
      }
      int result = UserDAO.getInstance().delete(deleteUser);
      if (result > 0) {
        log.info("Xoa user thanh cong");
        return true;
      }
      return false;
    } catch (EntityNotFoundException e) {
      throw new UserNotFoundException(userId);
    }
  }

  public User reset_password(int userId, String passWord)
      throws UserNotFoundException, DataException {
    log.info("Đang reset password cho user id={}", userId);
    try {
      User resetPW = UserDAO.getInstance().selectById(userId);
      if (resetPW == null) {
        log.warn("Không tìm thấy user để reset password: id={}", userId);
        throw new UserNotFoundException(userId);
      }
      User newUser =
          new User(
              userId,
              resetPW.getName(),
              PasswordHasher.hash(passWord),
              resetPW.getEmail(),
              resetPW.getSdt(),
              resetPW.getAccountType(),
              resetPW.getImagePath());

      int result = UserDAO.getInstance().update(newUser);
      if (result > 0) {
        log.info("Doi mat khau thanh cong");
        return newUser;
      }
      return null;
    } catch (EntityNotFoundException e) {
      log.warn("Không tìm thấy user để reset password: id={}", userId);
      throw new UserNotFoundException(userId);
    }
  }

  private static String mergeText(String currentValue, String newValue) {
    return newValue == null || newValue.isBlank() ? currentValue : newValue.trim();
  }
}
