package com.javfxtutorial.hethongdaugia.server.network;


import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
           clients.add(this);
            while (true) { //luôn chờ command của client
                Command cmd = (Command) in.readObject();
                Response rp = cmd.handle();
                out.writeObject(rp);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client ngừng kết nối");;
        } finally {
            // 3. Khi Client đóng app hoặc rớt mạng, xóa khỏi danh sách
            clients.remove(this);
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Client ngắt kết nối");
                }
            }
        }
    }

    public static void broadcast(Response rp) throws IOException {
        Iterator<ClientHandler> iterator = clients.iterator();
        while (iterator.hasNext()){
            ClientHandler client = iterator.next();
            try {
                client.out.writeObject(rp);
                client.out.flush();
            }  catch (IOException e){
                if (client.clientSocket != null){
                    client.clientSocket.close();
                }
                iterator.remove();
            }
        }
    }
}

// khi thao tác trên controller
// -> controller mở connection gửi command đến server -> server mở thread clienthandler
// -> clienthandler nhận command, gọi đến phương thức command.handle()
// (ở đây server không quan tâm Command loại gì, nó chỉ gọi đến phương tức handle, còn logic handle command ở trong phương thức rồi)
// phương thức này cũng chả về rp, xong server gửi lại client là xong



