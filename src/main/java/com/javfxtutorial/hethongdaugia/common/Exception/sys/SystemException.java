package com.javfxtutorial.hethongdaugia.common.Exception.sys;

import com.javfxtutorial.hethongdaugia.common.Exception.BaseException;
import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Base exception cho các lỗi hệ thống */
public class SystemException extends BaseException {
  private static final long serialVersionUID = 1L;

  public SystemException(ErrorCode errorCode) {
    super(errorCode.getCode(), errorCode.getDefaultMessage(), errorCode.getHttpStatusCode());
  }

  public SystemException(ErrorCode errorCode, String mesage) {
    super(errorCode.getCode(), mesage, errorCode.getHttpStatusCode());
  }

  public SystemException(ErrorCode errorCode, String mesage, Throwable cause) {
    super(mesage, cause, errorCode.getCode(), errorCode.getHttpStatusCode());
  }
}
