package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    public String IP = "localhost";
    public int PORT = 5000;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static ServerConnection instance;
    public static ServerConnection getInstance(){
        if (instance == null || !instance.isConnected()){
            instance = new ServerConnection();
        }
        return instance;
    }

    private ServerConnection(){
        //khởi tạo socket connect tới server và luông để truyền/ nhận dữ liệu
        try {
            this.clientSocket = new Socket(IP, PORT);
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Không kết nối được server");
        }
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.start();

    }

    // Gửi command và chờ response (đồng bộ)
    public synchronized void sendCommand(Command cmd) {
        try {
            out.writeObject(cmd);
            out.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Response receiveResponse() throws IOException, ClassNotFoundException {
        return (Response) in.readObject();
    }
    public void close() throws IOException {
        if (clientSocket != null) {
            this.clientSocket.close();
        }
    }

    public boolean isConnected(){
        if (instance.clientSocket == null){
            return false;
        }
        return true;
    }
}
