package com.javfxtutorial.hethongdaugia.common.Exception.bus;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class BusinessException extends BaseException {
  private static final long serialVersionUID = 1L;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode());
  }

  public BusinessException(ErrorCode errorCode, String mesage) {
    super(errorCode.getCode(), mesage, errorCode.getHttpStatusCode());
  }

  public BusinessException(ErrorCode errorCode, String mesage, Throwable cause) {
    super(mesage, cause, errorCode.getCode(), errorCode.getHttpStatusCode());
  }
}
