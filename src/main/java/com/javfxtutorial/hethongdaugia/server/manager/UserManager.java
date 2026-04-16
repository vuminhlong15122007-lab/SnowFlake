package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.common.model.User;

public class UserManager {
    private static UserManager instance;
    private UserManager(){}
    public static UserManager getInstance() {
        if (instance == null){
            instance = new UserManager();
        }
        return instance;
    }

    public User authenticate(String username, String password){
        User user = UserDAO.getInstance().selectByUsername(username);
        if (user != null && user.getPassWord().equals(password)){
            return user;
        }
        return null;
    }

    public boolean checkExistedUsername(String username){ //trả về true nếu username đã tồn tại
        User user = UserDAO.getInstance().selectByUsername(username);
        if (user != null){
            return true;
        }
        return false;
    }
    public User updateUserProfile(int userId, String username, String email, String phone){
        User oldUser = UserDAO.getInstance().selectById(userId);
        if(oldUser == null){
            return null;
        }
        //tao user moi tu thong tin cao nhat
        User updateUser = new User(userId, username, oldUser.getPassWord(), email, phone, oldUser.getAccountType());
        int result = UserDAO.getInstance().update(updateUser);
        if(result > 0){
            return updateUser; //thanh cong thi tra ve user moi
        }
        return null;//that bai
    }
    public boolean deleteUser(int userId, String username, String email, String phone){
        User deleteUser = UserDAO.getInstance().selectById(userId);
        int result = UserDAO.getInstance().delete(deleteUser);
        if (result > 0){
            System.out.println( "Xóa user thành công");
            return true;
        }
        return false;
    }
    public User reset_password(int userId, String passWord){
        User resetPW = UserDAO.getInstance().selectById(userId);
        //user moi
        User newUser = new User(userId, resetPW.getName(), passWord, resetPW.getEmail(), resetPW.getSdt(), resetPW.getAccountType());
        int result = UserDAO.getInstance().update(newUser);
        if (result > 0){
            System.out.println( "Đổi mật khẩu thành công");
            return newUser;
        }
        return null;
    }
}
