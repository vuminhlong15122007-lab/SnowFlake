package com.javfxtutorial.hethongdaugia.server.network;


import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.RequestType;
import com.javfxtutorial.hethongdaugia.common.network.Response;
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
            Response rp = handleCommand(cmd);
            out.writeObject(rp);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Response handleCommand(Command cmd) {
        switch (cmd.getRequestType()) {
            case CHECK_LOGIN:
                String username = (String) cmd.getData("username");
                String password = (String) cmd.getData("password");
                User user = UserManager.getInstance().authenticate(username, password);
                if (user != null) {
                    return new Response(true, "Đăng nhập thành công", user);
                }
                return new Response(false, "Sai tên hoặc mật khẩu", null);


        }
        return new Response(false, "Lỗi", null);
    }
}






