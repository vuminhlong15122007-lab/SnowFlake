package com.javfxtutorial.hethongdaugia.server.network;


import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread implements BidListener {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Luồng "+ this.getName() +" đang chạy");
            ClientHandlerContextHolder.set(this);
            System.out.println("Vừa khởi tạo "+ ClientHandlerContextHolder.get().getName());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) { //luôn chờ command của client
                Command cmd = (Command) in.readObject();
                Response rp = cmd.handle();
                if (rp != null) {
                    synchronized (out) {
                        out.writeObject(rp);
                        out.flush();
                        out.reset(); // Quan trọng: Tránh cache dữ liệu cũ
                    }
                } else {
                    System.err.println("Cảnh báo: Command " + cmd.getClass().getSimpleName() + " trả về null!");
                }
            }
        } catch (IOException | ClassNotFoundException e ) {
            System.out.println("Client ngừng kết nối");
            try {
                clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            // 3. Khi Client đóng app hoặc rớt mạng, xóa khỏi danh sách
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Client ngắt kết nối");
                }
            }
        }
    }

    @Override
    public void onPlaceBid(BidTransaction bid, ClientHandler senderThread) { // chỉ gửi cho người kph sender
        Response rp = null;
        if (!senderThread.equals(this)) {
            rp = new Response(true, "Có người mới đặt giá", bid, new PlaceBidCommand());
            try {
                out.writeObject(rp);
                System.out.println("Đã gửi PlaceBidCommand về cho luồng" + this.getName());
            } catch (IOException e) {
                System.out.println("Lỗi outputStream");
            }
        }
    }
}

// khi thao tác trên controller
// -> controller mở connection gửi command đến server -> server mở thread clienthandler
// -> clienthandler nhận command, gọi đến phương thức command.handle()
// (ở đây server không quan tâm Command loại gì, nó chỉ gọi đến phương tức handle, còn logic handle command ở trong phương thức rồi)
// phương thức này cũng chả về rp, xong server gửi lại client là xong



