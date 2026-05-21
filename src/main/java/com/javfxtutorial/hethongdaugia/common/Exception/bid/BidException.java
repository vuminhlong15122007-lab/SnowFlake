package com.javfxtutorial.hethongdaugia.common.Exception.bid;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class BidException extends BaseException {
    private static final long serialVersionUID = 1L;

    public BidException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode());
    }

    public BidException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), message, errorCode.getHttpStatusCode());
    }

    public BidException(ErrorCode errorCode, Throwable cause) {
        super(
                errorCode.getDefaultMessage(),
                cause,
                errorCode.getCode(),
                errorCode.getHttpStatusCode());
    }
}
