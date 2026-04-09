package com.javfxtutorial.hethongdaugia.server.network;


import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            Command cmd = (Command) in.readObject();
            Response rp = cmd.handle();
            out.writeObject(rp);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

// khi thao tác trên controller
// -> controller mở connection gửi command đến server -> server mở thread clienthandler
// -> clienthandler nhận command, gọi đến phương thức command.handle()
// (ở đây server không quan tâm Command loại gì, nó chỉ gọi đến phương tức handle, còn logic handle command ở trong phương thức rồi)
// phương thức này cũng chả về rp, xong server gửi lại client là xong



