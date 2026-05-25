package com.javfxtutorial.hethongdaugia.server;

import com.javfxtutorial.hethongdaugia.server.dao.NotificationDAO;
import com.javfxtutorial.hethongdaugia.server.handler.GlobalExceptionHandler;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp {
    private static final Logger log = LoggerFactory.getLogger(ServerApp.class);
    private static final int PORT = 5000;
    private static volatile int userCount = 0;

    public static void main(String[] args) {
        GlobalExceptionHandler.register();

        // Dọn notification hết hạn mỗi 12 tiếng
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> NotificationDAO.getInstance().deleteExpired(), 0, 12, TimeUnit.HOURS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log.info("Server đã khởi động, đang lắng nghe trên cổng {}", PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("Client kết nối từ: {}", clientSocket.getInetAddress());
                // Mỗi client được xử lý trong một thread riêng
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            log.error("Lỗi server: {}", e.getMessage(), e);
        }
    }
}