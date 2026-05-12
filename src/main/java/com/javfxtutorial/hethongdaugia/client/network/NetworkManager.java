package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;

import static java.lang.Thread.sleep;

public class NetworkManager {
  private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
  Map<Class<?>, List<ResponseListener>> listeners;
  private static NetworkManager instance;

  private NetworkManager() {
    listeners = new ConcurrentHashMap<>();
  }

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
    } catch (IOException e) {
      log.error("Không tìm thấy server: {}", e.getMessage());
    }
    return connection;
  }

//  public void register(Class<?> commandClass, ResponseListener listener) {
//    if (listeners.containsKey(commandClass)) {
//      listeners.get(commandClass).add(listener);
//    } else {
//      listeners.put(commandClass, new CopyOnWriteArrayList<>(List.of(listener)));
//    }
//  }

  public void register(Class<?> commandClass, ResponseListener listener) {
    listeners.computeIfAbsent(commandClass, k -> new CopyOnWriteArrayList<>());

    List<ResponseListener> list = listeners.get(commandClass);
    if (!list.contains(listener)) {
      list.add(listener);
    }
  }

  public void unregister(Class<?> commandClass, ResponseListener listener) {
    List<ResponseListener> list = listeners.get(commandClass);
    if (list != null) list.remove(listener);
  }

  public void start() {
    Thread thread = new Thread(() -> {
      while (true) {
        ServerConnection connection = null;
        try {
          connection = ServerConnection.getInstance();
        } catch (IOException e) {
          log.error("Không tìm thấy kết nối server: {}", e.getMessage());
          try { sleep(2000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; }
          continue;
        }

        Response rp = null;
        try {
          rp = connection.receiveResponse();
        } catch (IOException | ClassNotFoundException e) {
          // In ra lỗi thật để debug
          log.error("Lỗi khi đọc response: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
          continue;
        }

        if (rp == null) {
          log.warn("Response nhận được là null, bỏ qua");
          continue;
        }

        try {
          Class<?> commandType = rp.getCommand().getClass();
          List<ResponseListener> list = listeners.get(commandType);
          if (list != null) {
            for (ResponseListener listener : list) {
              if (listener != null) {
                listener.onResponse(rp);
              }
            }
          } else {
            log.warn("Không có listener nào cho command: {}", commandType.getSimpleName());
          }
        } catch (NullPointerException e) {
          log.error("NullPointerException khi dispatch response: {}", e.getMessage(), e);
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }
}
