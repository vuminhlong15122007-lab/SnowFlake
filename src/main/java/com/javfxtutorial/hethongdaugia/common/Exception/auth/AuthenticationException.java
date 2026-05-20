package com.javfxtutorial.hethongdaugia.common.Exception.auth;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class AuthenticationException extends BaseException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode());
    }

    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), message, errorCode.getHttpStatusCode());
    }

    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(
                errorCode.getDefaultMessage(),
                cause,
                errorCode.getCode(),
                errorCode.getHttpStatusCode());
    }

    public AuthenticationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode.getCode(), errorCode.getHttpStatusCode());
    }
}
