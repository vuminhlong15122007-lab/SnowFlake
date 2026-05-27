package com.javfxtutorial.hethongdaugia.client.network;

import static java.lang.Thread.sleep;

import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkManager {
  private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
  private final Map<Class<?>, List<ResponseListener>> listeners;
  private final Map<String, ResponseListener> pendingRequests;
  private static NetworkManager instance;
  private volatile boolean started;

  private NetworkManager() {
    listeners = new ConcurrentHashMap<>();
    pendingRequests = new ConcurrentHashMap<>();
  }

  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
    }
    return instance;
  }

  public static ServerConnection getConnection() throws ConnectionFailedException {
    ServerConnection connection;
    try {
      connection = ServerConnection.getInstance();
      return connection;
    } catch (IOException e) {
      log.error("Khong tim thay server: {}", e.getMessage());
      throw new ConnectionFailedException("localhost", e);
    }
  }

  public void register(Class<?> commandClass, ResponseListener listener) {
    listeners.computeIfAbsent(commandClass, _ -> new CopyOnWriteArrayList<>());

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
    Thread thread =
        new Thread(
            () -> {
              while (true) {
                ServerConnection connection = null;
                try {
                  connection = ServerConnection.getInstance();
                } catch (IOException e) {
                  log.error("Khong tim thay ket noi server: {}", e.getMessage());
                  if (connection != null) {
                    connection.close();
                  }
                  try {
                    sleep(2000);
                  } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                  }
                  continue;
                }

                Response rp;
                try {
                  rp = connection.receiveResponse();
                } catch (IOException | ClassNotFoundException e) {
                  log.error(
                      "Loi khi doc response: {} - {}",
                      e.getClass().getSimpleName(),
                      e.getMessage(),
                      e);
                  connection.close();
                  try {
                    sleep(500);
                  } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                  }
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

                String requestId = rp.getRequestId();
                if (requestId != null) {
                  ResponseListener listener = pendingRequests.remove(requestId);
                  if (listener != null) {
                    listener.onResponse(rp);
                  } else {
                    log.warn("Khong co pending listener cho requestId: {}", requestId);
                  }
                  continue;
                }

                Class<?> commandType = rp.getCommand().getClass();
                List<ResponseListener> list = listeners.get(commandType);

                if (list != null) {
                  for (ResponseListener listener : list) {
                    if (listener != null) {
                      try {
                        listener.onResponse(rp);
                      } catch (Exception e) {
                        log.error(
                            "Listener {} crash khi xử lý response: {}",
                            listener.getClass().getSimpleName(),
                            e.getMessage(),
                            e);
                      }
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

  public void sendRequest(Command cmd, ResponseListener listener)
      throws ConnectionFailedException, SendFailedException {
    String requestId = java.util.UUID.randomUUID().toString();
    cmd.setRequestId(requestId);
    if (listener != null) {
      pendingRequests.put(requestId, listener);
    }

    try {
      getConnection().sendCommand(cmd);
    } catch (ConnectionFailedException | SendFailedException | RuntimeException e) {
      pendingRequests.remove(requestId);
      throw e;
    }
  }
}
