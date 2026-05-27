package com.javfxtutorial.hethongdaugia.common.Exception.net;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Lỗi timeout khi kết nối */
public class ConnectionTimeoutException extends NetworkException {
  private static final long serialVersionUID = 1L;
  private final int timeoutSeconds;
  private final String serverAddress;

  public ConnectionTimeoutException(String serverAddress, int timeoutSeconds, Throwable cause) {
    super(
        ErrorCode.NET_CONNECTION_TIMEOUT,
        String.format("Kết nối đến %s timeout sau %d giây", serverAddress, timeoutSeconds),
        cause);
    this.serverAddress = serverAddress;
    this.timeoutSeconds = timeoutSeconds;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public String getServerAddress() {
    return serverAddress;
  }
}
