package com.javfxtutorial.hethongdaugia.common.Exception.net;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Lỗi nhận dữ liệu thất bại */
public class ReceiveFailedException extends NetworkException {
  private static final long serialVersionUID = 1L;
  private final String dataType;

  public ReceiveFailedException(String dataType) {
    super(ErrorCode.NET_SEND_FAILED, String.format("Nhận %s thất bại", dataType));
    this.dataType = dataType;
  }

  public String getDataType() {
    return dataType;
  }
}
