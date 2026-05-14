package com.javfxtutorial.hethongdaugia.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    private static final int PORT = 5000;
    private static volatile int userCount = 0;

     public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đã khởi động, đang lắng nghe trên cổng " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client kết nối từ: " + clientSocket.getInetAddress());
                // Mỗi client được xử lý trong một thread riêng
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
