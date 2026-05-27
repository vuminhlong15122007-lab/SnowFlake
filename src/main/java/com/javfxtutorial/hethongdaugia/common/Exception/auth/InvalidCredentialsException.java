package com.javfxtutorial.hethongdaugia.common.Exception.auth;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

// sai ten dang nhap hoac mat khau
public class InvalidCredentialsException extends AuthenticationException {
  private static final long serialVersionUID = 1L;

  public InvalidCredentialsException() {
    super(ErrorCode.AUTH_INVALID_CREDENTIALS);
  }

  public InvalidCredentialsException(String message) {
    super(ErrorCode.AUTH_INVALID_CREDENTIALS, message);
  }

  public InvalidCredentialsException(Throwable cause) {
    super(ErrorCode.AUTH_INVALID_CREDENTIALS, cause);
  }
}
