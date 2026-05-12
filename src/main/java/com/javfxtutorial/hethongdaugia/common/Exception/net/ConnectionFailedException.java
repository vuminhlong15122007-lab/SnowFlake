package com.javfxtutorial.hethongdaugia.common.Exception.net;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Lỗi mất kêt nối đến server
 */
public class ConnectionFailedException extends NetworkException {
  private static final long serialVersionUID = 1L;
  private final String serverAddress;

  public ConnectionFailedException(String serverAddress, Throwable cause){
    super(ErrorCode.NET_CONNECTION_LOST,
            String.format("Mất kết nối đến server %s", serverAddress), cause);
    this.serverAddress = serverAddress;
  }

  public String getServerAddress() {
    return serverAddress;
  }
}
