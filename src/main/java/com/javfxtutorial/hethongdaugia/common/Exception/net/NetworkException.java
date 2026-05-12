package com.javfxtutorial.hethongdaugia.common.Exception.net;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Base exception cho các lỗi mạng
 */
public class NetworkException extends BaseException {
    private static final long serialVersionUID = 1L;

    public NetworkException(ErrorCode errorCode){
        super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode());
    }

    public NetworkException(ErrorCode errorCode, String mesage){
        super(errorCode.getCode(), mesage, errorCode.getHttpStatusCode());
    }

    public NetworkException(ErrorCode errorCode, String mesage, Throwable cause){
        super(mesage, cause, errorCode.getCode(), errorCode.getHttpStatusCode());
    }
}
