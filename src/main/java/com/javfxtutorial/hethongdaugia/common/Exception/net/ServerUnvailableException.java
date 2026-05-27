package com.javfxtutorial.hethongdaugia.common.Exception.net;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Lỗi server không khả dụng */
public class ServerUnvailableException extends NetworkException {
  private final String serverAddress;
  private final String reason;

  public ServerUnvailableException(String serverAddress, int port, String reason) {
    super(
        ErrorCode.NET_SERVER_UNVAILABLE,
        String.format("Server %s hiện không khả dụng: %s", serverAddress, reason));
    this.reason = reason;
    this.serverAddress = serverAddress;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public String getReason() {
    return reason;
  }
}
