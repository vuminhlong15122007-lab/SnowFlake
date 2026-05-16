package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class NetworkManager {
  private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
  Map<Class<?>, List<ResponseListener>> listeners;
  private static NetworkManager instance;
  private volatile boolean started;

  private NetworkManager() {
    listeners = new ConcurrentHashMap<>();
  }

  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
    }
    return instance;
  }

  public static ServerConnection getConnection() throws ConnectionFailedException{
    try {
      return ServerConnection.getInstance();
    } catch (IOException e) {
      log.error("Khong tim thay server: {}", e.getMessage());
      throw new ConnectionFailedException("localhost", e);
    }
  }

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

  public synchronized void start() {
    if (started) {
      return;
    }
    started = true;

    Thread thread = new Thread(() -> {
      while (true) {
        ServerConnection connection;
        try {
          connection = ServerConnection.getInstance();
        } catch (IOException e) {
          log.error("Khong tim thay ket noi server: {}", e.getMessage());
          try { sleep(2000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); started = false; return; }
          continue;
        }

        Response rp;
        try {
          rp = connection.receiveResponse();
        } catch (IOException | ClassNotFoundException e) {
          log.error("Loi khi doc response: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
          try {
            connection.close();
          } catch (IOException closeException) {
            log.warn("Khong the dong ket noi loi: {}", closeException.getMessage());
          }
          try { sleep(500); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); started = false; return; }
          continue;
        }

        if (rp == null) {
          log.warn("Response nhan duoc la null, bo qua");
          continue;
        }

        if (rp.getCommand() == null) {
          log.warn("Response khong co command, bo qua: {}", rp.getMessage());
          continue;
        }

        Class<?> commandType = rp.getCommand().getClass();
        List<ResponseListener> list = listeners.get(commandType);
        if (list != null) {
          for (ResponseListener listener : list) {
            if (listener != null) {
              listener.onResponse(rp);
            }
          }
        } else {
          log.warn("Khong co listener nao cho command: {}", commandType.getSimpleName());
        }
      }
    });
    thread.setDaemon(true);
    thread.setName("client-network-response-reader");
    thread.start();
  }
}
