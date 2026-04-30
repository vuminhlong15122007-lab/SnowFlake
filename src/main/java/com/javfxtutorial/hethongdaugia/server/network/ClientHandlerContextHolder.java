package com.javfxtutorial.hethongdaugia.server.network;

public class ClientHandlerContextHolder {
    // khởi tạo một ThreadLocal
    private static final ThreadLocal<ClientHandler> currentThread = new ThreadLocal<>();

    public static void set(ClientHandler userThread) {
        currentThread.set(userThread);
    }

    public static ClientHandler get() {
        return currentThread.get();
    }

    public static void clear() {
        currentThread.remove(); // cực kỳ quan trọng để dọn dẹp
    }
}
