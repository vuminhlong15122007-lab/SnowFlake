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

/**
 * Quản lý nghiệp vụ liên quan đến tài khoản người dùng ở phía server.
 *
 * <p>Class này đứng giữa tầng network/command và UserDAO, chịu trách nhiệm xác thực đăng nhập, cập
 * nhật thông tin cá nhân, xóa user và reset mật khẩu.
 */
public class UserManager {
  private static final Logger log = LoggerFactory.getLogger(UserManager.class);

  // Singleton dùng chung cho toàn server; volatile giúp an toàn khi nhiều thread truy cập.
  private static volatile UserManager instance;

  private UserManager() {}

  /**
   * Trả về instance duy nhất của UserManager.
   *
   * <p>Input: không có. Output: singleton UserManager. Luồng xử lý: chỉ tạo mới khi instance chưa có
   * và dùng synchronized để tránh nhiều thread tạo trùng.
   */
  public static UserManager getInstance() {
    // Nếu instance đã tồn tại thì trả về ngay, không cần khóa.
    if (instance == null) {
      synchronized (UserManager.class) {
        // Kiểm tra lại sau khi vào synchronized để đảm bảo chỉ tạo một instance.
        if (instance == null) {
          instance = new UserManager();
        }
      }
    }
    return instance;
  }

  /**
   * Xác thực tài khoản đăng nhập.
   *
   * <p>Input: username và password người dùng nhập. Output: User hợp lệ nếu đăng nhập thành công.
   * Luồng xử lý: tìm user theo username, kiểm tra mật khẩu, tự nâng cấp mật khẩu chưa hash sang dạng
   * hash, rồi trả về user. Nếu sai tài khoản/mật khẩu thì ném InvalidCredentialsException.
   */
  public User authenticate(String username, String password)
      throws InvalidCredentialsException, DataException {
    try {
      // Đọc user từ database theo username để lấy password đã lưu.
      User user = UserDAO.getInstance().selectByUsername(username);
      // Không tìm thấy username thì coi như thông tin đăng nhập không hợp lệ.
      if (user == null) {
        log.warn("Không tìm thấy user: {}", username);
        throw new InvalidCredentialsException();
      }
      // Mật khẩu nhập vào không khớp mật khẩu lưu trong DB thì từ chối đăng nhập.
      if (!PasswordHasher.matches(password, user.getPassWord())) {
        log.warn("Mật khẩu sai cho user: {}", username);
        throw new InvalidCredentialsException();
      }

      // Nếu tài khoản cũ còn lưu password dạng plain text thì hash lại sau khi xác thực thành công.
      if (!PasswordHasher.isHashed(user.getPassWord())) {
        user.setPassWord(PasswordHasher.hash(password));
        UserDAO.getInstance().update(user);
      }
      log.info("Xác thực thành công: {}", username);
      return user;
    } catch (EntityNotFoundException e) {
      // DAO có thể báo không tìm thấy bằng exception; manager chuyển thành lỗi đăng nhập chung.
      log.warn("Không tìm thấy user: {}", username);
      throw new InvalidCredentialsException();
    }
  }

  /**
   * Cập nhật thông tin cá nhân của user.
   *
   * <p>Input: userId và các trường mới có thể null/rỗng. Output: User sau khi cập nhật nếu DB update
   * thành công, null nếu không tìm thấy user hoặc update không ảnh hưởng dòng nào. Luồng xử lý: đọc
   * user cũ, merge từng trường mới với dữ liệu cũ, tạo object User mới rồi gọi DAO update.
   */
  public User updateUserProfile(int userId, String username, String email, String phone, String avt)
      throws UserNotFoundException, DataException {
    log.info("Cập nhật profile: userId={}", userId);
    try {
      // Cần lấy user hiện tại để giữ lại các trường không được client gửi lên.
      User oldUser = UserDAO.getInstance().selectById(userId);
      // DAO trả null nghĩa là không có user tương ứng để cập nhật.
      if (oldUser == null) {
        return null;
      }

      // Các biến resolved giữ giá trị cuối cùng sau khi ưu tiên dữ liệu mới nếu hợp lệ.
      String resolvedName = mergeText(oldUser.getName(), username);
      String resolvedEmail = mergeText(oldUser.getEmail(), email);
      String resolvedPhone = mergeText(oldUser.getSdt(), phone);
      // Avatar cho phép giá trị rỗng, nhưng null nghĩa là giữ avatar cũ.
      String resolvedAvatar = avt == null ? oldUser.getImagePath() : avt;

      // Tạo object mới với userId cũ và các trường đã merge để gửi xuống DAO.
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
      // DAO trả số dòng bị ảnh hưởng > 0 nghĩa là cập nhật thành công.
      if (result > 0) {
        return updateUser;
      }
      return null;
    } catch (EntityNotFoundException e) {
      // Chuyển lỗi không tìm thấy ở tầng DAO thành exception nghiệp vụ của manager.
      throw new UserNotFoundException(userId);
    }
  }

  /**
   * Xóa một user theo userId.
   *
   * <p>Input: userId là khóa chính cần xóa; username/email/phone hiện không được dùng trong logic
   * hiện tại. Output: true nếu xóa thành công, false nếu user không tồn tại hoặc DAO không xóa dòng
   * nào.
   */
  public boolean deleteUser(int userId, String username, String email, String phone)
      throws UserNotFoundException, DataException {
    try {
      // Đọc user trước để DAO delete nhận đúng object cần xóa.
      User deleteUser = UserDAO.getInstance().selectById(userId);
      // Không tìm thấy user thì trả false thay vì gọi delete.
      if (deleteUser == null) {
        return false;
      }
      int result = UserDAO.getInstance().delete(deleteUser);
      // Số dòng bị ảnh hưởng > 0 nghĩa là database đã xóa user.
      if (result > 0) {
        log.info("Xoa user thanh cong");
        return true;
      }
      return false;
    } catch (EntityNotFoundException e) {
      // DAO báo không tìm thấy thì chuyển sang lỗi nghiệp vụ theo userId.
      throw new UserNotFoundException(userId);
    }
  }

  /**
   * Đặt lại mật khẩu cho user.
   *
   * <p>Input: userId cần reset và passWord mới từ request. Output: User mới sau khi cập nhật mật
   * khẩu nếu thành công, null nếu DB không cập nhật dòng nào. Mật khẩu được đưa qua
   * PasswordHasher.hash trước khi lưu.
   */
  public User reset_password(int userId, String passWord)
      throws UserNotFoundException, DataException {
    log.info("Đang reset password cho user id={}", userId);
    try {
      // Lấy user hiện tại để giữ nguyên thông tin khác ngoài mật khẩu.
      User resetPW = UserDAO.getInstance().selectById(userId);
      // Không có user thì báo lỗi không tìm thấy cho caller.
      if (resetPW == null) {
        log.warn("Không tìm thấy user để reset password: id={}", userId);
        throw new UserNotFoundException(userId);
      }
      // Tạo object mới giữ nguyên thông tin cũ, chỉ thay password bằng kết quả PasswordHasher.hash.
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
      // Update thành công thì trả về user đã chứa mật khẩu hash mới.
      if (result > 0) {
        log.info("Doi mat khau thanh cong");
        return newUser;
      }
      return null;
    } catch (EntityNotFoundException e) {
      // DAO ném lỗi không tìm thấy thì manager trả về UserNotFoundException thống nhất.
      log.warn("Không tìm thấy user để reset password: id={}", userId);
      throw new UserNotFoundException(userId);
    }
  }

  /**
   * Gộp giá trị cũ và giá trị mới cho các trường text khi cập nhật profile.
   *
   * <p>Input: currentValue đang lưu trong DB và newValue từ request. Output: currentValue nếu newValue
   * null/rỗng, ngược lại trả về newValue đã trim khoảng trắng hai đầu.
   */
  private static String mergeText(String currentValue, String newValue) {
    // Client không gửi dữ liệu mới thì giữ nguyên giá trị hiện tại.
    return newValue == null || newValue.isBlank() ? currentValue : newValue.trim();
  }
}
