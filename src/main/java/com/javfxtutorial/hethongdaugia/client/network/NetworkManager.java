package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class NetworkManager {
    Map<Class<?>, List<ResponseListener>> listeners;
    private static NetworkManager instance;
    private NetworkManager(){
        listeners = new ConcurrentHashMap<>();
    };

    public static NetworkManager getInstance(){
        if (instance == null){
            instance = new NetworkManager();
        }
        return instance;
    }

    public void register(Class<?> commandClass, ResponseListener listener){
        if (listeners.containsKey(commandClass)){ // nếu đã có cmd tồn tại thì thêm vào list th
            listeners.get(commandClass).add(listener);
        }
        else {
            listeners.put(commandClass, new CopyOnWriteArrayList<>(List.of(listener)));
        }
    }
    public void unregister(Class<?> commandClass, ResponseListener listener){
        listeners.get(commandClass).remove(listener);
    }

    public void start() {
        Thread thread = new Thread(() -> {
            while (true) {
                Response rp = null;
                try {
                    rp = ServerConnection.getInstance().receiveResponse();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Không kết nối được server");
                    break;
                }
                try {
                    Class<?> commandType = rp.getCommand().getClass();
                    if (listeners != null) {
                        for (ResponseListener listener : listeners.get(commandType)) {
                            if (listener != null) {
                                listener.onResponse(rp);
                            }
                        }
                    }
                }catch(Exception e ){
                    System.out.println("Giá trị trả về là null");
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

}
