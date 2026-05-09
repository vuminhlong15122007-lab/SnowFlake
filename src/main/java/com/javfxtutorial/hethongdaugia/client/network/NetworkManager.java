package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class NetworkManager {
  private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
  Map<Class<?>, List<ResponseListener>> listeners;
  private static NetworkManager instance;

  private NetworkManager() {
    listeners = new ConcurrentHashMap<>();
  }

  ;

  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
    }
    return instance;
  }

  public static ServerConnection getConnection() {
    ServerConnection connection = null;
    try {
      connection = ServerConnection.getInstance();
    } catch (IOException _) {
      log.error("Không tìm thấy server");
    }
    return connection;
  }

  public void register(Class<?> commandClass, ResponseListener listener) {
    if (listeners.containsKey(commandClass)) { // nếu đã có cmd tồn tại thì thêm vào list th
      listeners.get(commandClass).add(listener);
    } else {
      listeners.put(commandClass, new CopyOnWriteArrayList<>(List.of(listener)));
    }
  }

  public void unregister(Class<?> commandClass, ResponseListener listener) {
    listeners.get(commandClass).remove(listener);
  }

  public void start() {
    Thread thread = new Thread(() -> {
      while (true) {
        ServerConnection connection = null;
        try {
          connection = ServerConnection.getInstance();
        } catch (IOException e) {
          try {
            log.error("Không tìm thấy kết nối server");
            sleep(2000);
            continue;
          } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
          }
        }


        Response rp = null;
        try {
          rp = connection.receiveResponse();
        } catch (IOException | ClassNotFoundException e) {
          log.error("Lỗi khi đọc response");
          continue;
        }
        // BƯỚC 2: Tìm ai đang quan tâm response này
        Class<?> commandType = rp.getCommand().getClass();
        if (listeners != null) {
          for (ResponseListener listener : listeners.get(commandType)) {
            if (listener != null) {
              listener.onResponse(rp);
            }
          }
        }
      }

    });
    thread.setDaemon(true);
    thread.start();
  }

}
