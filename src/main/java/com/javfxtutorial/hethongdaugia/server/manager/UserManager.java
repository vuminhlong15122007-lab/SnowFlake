package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.security.PasswordHasher;

public class UserManager {
    private static UserManager instance;

    private UserManager() {}

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User authenticate(String username, String password) {
        User user = UserDAO.getInstance().selectByUsername(username);
        if (user != null && PasswordHasher.matches(password, user.getPassWord())) {
            if (!PasswordHasher.isHashed(user.getPassWord())) {
                user.setPassWord(password);
                UserDAO.getInstance().update(user);
            }
            return user;
        }
        return null;
    }

    public User updateUserProfile(int userId, String username, String email, String phone, String avt) {
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
        return null;
    }

    public boolean deleteUser(int userId, String username, String email, String phone) {
        User deleteUser = UserDAO.getInstance().selectById(userId);
        int result = UserDAO.getInstance().delete(deleteUser);
        if (result > 0) {
            System.out.println("Xoa user thanh cong");
            return true;
        }
        return false;
    }

    public User reset_password(int userId, String passWord) {
        User resetPW = UserDAO.getInstance().selectById(userId);
        User newUser = new User(
                userId,
                resetPW.getName(),
                passWord,
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
        return null;
    }

    public boolean checkExistedUsername(String username1) {
        return UserDAO.getInstance().selectByUsername(username1) != null;
    }
}
