package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.EntityNotFoundException;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManager {
    private static final Logger log = LoggerFactory.getLogger(UserManager.class);

    private static UserManager instance;

    private UserManager() {}

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User authenticate(String username, String password)  throws InvalidCredentialsException, DataException{
        User user = UserDAO.getInstance().selectByUsername(username);
        if (user == null) {
            log.warn("Không tìm thấy user: {}", username);
            throw new InvalidCredentialsException();
        }
        if (!PasswordHasher.matches(password, user.getPassWord())) {
            log.warn("Mật khẩu sai cho user: {}", username);
            throw new InvalidCredentialsException();
        }

        if (user != null && PasswordHasher.matches(password, user.getPassWord())) {
            if (!PasswordHasher.isHashed(user.getPassWord())) {
                user.setPassWord(PasswordHasher.hash(password));
                UserDAO.getInstance().update(user);
            }
            log.info("Xác thực thành công: {}", username);
            return user;
        }
        return null;
    }

    public User updateUserProfile(int userId, String username, String email, String phone, String avt) throws UserNotFoundException, DataException {
        log.info("Cập nhật profile: userId={}", userId);
    try{
        User oldUser = UserDAO.getInstance().selectById(userId);
        if (oldUser == null) {
            return null;
        }

        String resolvedName = username == null || username.isBlank() ? oldUser.getName() : username.trim();
        String resolvedEmail = email == null || email.isBlank() ? oldUser.getEmail() : email.trim();
        String resolvedPhone = phone == null || phone.isBlank() ? oldUser.getSdt() : phone.trim();
        String resolvedAvatar = avt == null ? oldUser.getImagePath() : avt;

        User updateUser = new User(userId, resolvedName, oldUser.getPassWord(), resolvedEmail, resolvedPhone, oldUser.getAccountType(), resolvedAvatar);

        int result = UserDAO.getInstance().update(updateUser);
        if (result > 0) {
            return updateUser;
        }
        return null;} catch (EntityNotFoundException e){
        throw new UserNotFoundException(userId);
        }
    }

    public boolean deleteUser(int userId, String username, String email, String phone) throws UserNotFoundException, DataException{
        try{
            User deleteUser = UserDAO.getInstance().selectById(userId);
            int result = UserDAO.getInstance().delete(deleteUser);
            if (result > 0) {
                System.out.println("Xoa user thanh cong");
                return true;
            }
            return false;} catch (EntityNotFoundException e){
            throw new UserNotFoundException(userId);
        }
    }

    public User reset_password(int userId, String passWord) throws UserNotFoundException, DataException {
        log.info("Đang reset password cho user id={}", userId);
        try{
            User resetPW = UserDAO.getInstance().selectById(userId);
            if (resetPW == null) {
                log.warn("Không tìm thấy user để reset password: id={}", userId);
                throw new UserNotFoundException(userId);
            }
            User newUser = new User(
                    userId,
                    resetPW.getName(),
                    PasswordHasher.hash(passWord),
                    resetPW.getEmail(),
                    resetPW.getSdt(),
                    resetPW.getAccountType(),
                    resetPW.getImagePath()
            );

            int result = UserDAO.getInstance().update(newUser);
            if (result > 0) {
                System.out.println("Doi mat khau thanh cong");
                    return newUser;
            }
            return null;}catch (EntityNotFoundException e) {
            log.warn("Không tìm thấy user để reset password: id={}", userId);
            throw new UserNotFoundException(userId);
        }
    }

}
