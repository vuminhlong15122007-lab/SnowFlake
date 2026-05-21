package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class DataException extends BaseException {
    /** Base exception cho các lỗi truy cập dữ liệu */
    private static final long serialVersionUID = 1L;

    public DataException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode());
    }

    public DataException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), message, errorCode.getHttpStatusCode());
    }

    public DataException(ErrorCode errorCode, Throwable cause) {
        super(
                errorCode.getDefaultMessage(),
                cause,
                errorCode.getCode(),
                errorCode.getHttpStatusCode());
    }

    public DataException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause, errorCode.getCode(), errorCode.getHttpStatusCode());
    }
}
