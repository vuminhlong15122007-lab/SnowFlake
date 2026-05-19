package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
  private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);

  public String IP = "10.11.6.209";
  public int PORT = 5000;
  private final Socket clientSocket;
  private final ObjectOutputStream out;
  private final ObjectInputStream in;
  private static ServerConnection instance;

  public static ServerConnection getInstance() throws IOException {
    if (instance == null || !instance.isConnected()) {
      instance = new ServerConnection();
    }
    return instance;
  }

  private ServerConnection() throws IOException {
    //khởi tạo socket connect tới server và luông để truyền/ nhận dữ liệu
    this.clientSocket = new Socket(IP, PORT);
    this.out = new ObjectOutputStream(clientSocket.getOutputStream());
    this.in = new ObjectInputStream(clientSocket.getInputStream());
  }

  // Gửi command và chờ response (đồng bộ)
  public synchronized void sendCommand(Command cmd) throws SendFailedException {
    try {
      out.writeObject(cmd);
      out.flush();
      out.reset();
    } catch (IOException e) {
      log.error("Gửi command thất bại: {}", e.getMessage(), e);
      throw new SendFailedException(cmd.getClass().getSimpleName());
    }
  }

  public Response receiveResponse() throws IOException, ClassNotFoundException {
    return (Response) in.readObject();
  }

  public void close() {
    try {
      if (clientSocket != null) clientSocket.close();
    } catch (IOException e) {
      log.error("Lỗi khi đóng socket: {}", e.getMessage());
    } finally {
      instance = null;
    }
  }

  public boolean isConnected() {
    return clientSocket != null &&
        clientSocket.isConnected() &&
        !clientSocket.isClosed() &&
        !clientSocket.isInputShutdown();  }
}
