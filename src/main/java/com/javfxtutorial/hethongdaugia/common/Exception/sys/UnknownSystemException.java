package com.javfxtutorial.hethongdaugia.common.Exception.sys;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class UnknownSystemException extends SystemException {
  private static final long serialVersionUID = 1L;

  public UnknownSystemException() {
    super(
        ErrorCode.SYS_UNKNOW_ERROR, "Đã xảy ra lỗi không xác định, vui lòng liên hệ quản trị viên");
  }

  public UnknownSystemException(String message) {
    super(ErrorCode.SYS_UNKNOW_ERROR, message);
  }

  public UnknownSystemException(Throwable cause) {
    super(ErrorCode.SYS_UNKNOW_ERROR, "Lỗi: " + cause.getMessage(), cause);
  }

  public UnknownSystemException(String message, Throwable cause) {
    super(ErrorCode.SYS_UNKNOW_ERROR, message, cause);
  }
}
