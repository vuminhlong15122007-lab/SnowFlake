package com.javfxtutorial.hethongdaugia.common.Exception.auth;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class UserNotFoundException extends AuthenticationException {
    private static final long serialVersionUID = 1L;
    private final int userId;
    public UserNotFoundException(int userId){
        super(ErrorCode.AUTH_USER_NOTFOUND, "Không tìm thấy tài khoản " + userId);
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}
