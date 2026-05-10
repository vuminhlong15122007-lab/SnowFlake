package com.javfxtutorial.hethongdaugia.common.Exception.auc;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class AuctionException extends BaseException {
    private static final long serialVersionUID = 1L;

    public AuctionException(ErrorCode errorCode){
        super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode() );
    }

    public AuctionException(ErrorCode errorCode, String message){
        super(errorCode.getCode(), message, errorCode.getHttpStatusCode());
    }

    public AuctionException(ErrorCode errorCode, Throwable cause){
        super(errorCode.getDefaultMessage(), cause, errorCode.getCode(), errorCode.getHttpStatusCode());
    }
}
