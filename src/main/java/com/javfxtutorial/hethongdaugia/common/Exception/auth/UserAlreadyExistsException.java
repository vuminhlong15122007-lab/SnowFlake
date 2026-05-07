package com.javfxtutorial.hethongdaugia.common.Exception.auth;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class UserAlreadyExistsException extends AuthenticationException {
    private static final long serialVersionUID = 1L;
    private final String username;
    public UserAlreadyExistsException(String username){
        super(ErrorCode.AUTH_USER_ALREADY_EXISTS, "Tài khoản " + username + " đã tồn tại");
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
