package com.javfxtutorial.hethongdaugia.common.Exception;

import java.io.Serializable;

public abstract class BaseException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final int httpStatusCode;

    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = 400; // mac dinh loi client(sai du lieu)
    }

    public BaseException(String errorCode, String message, int httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public BaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = 400;
    }

    public BaseException(String message, Throwable cause, String errorCode, int httpStatusCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String toString() {
        return "[ "
                + this.errorCode
                + " ] "
                + this.getMessage()
                + " (HTTP: "
                + this.httpStatusCode
                + " )";
    }
}
