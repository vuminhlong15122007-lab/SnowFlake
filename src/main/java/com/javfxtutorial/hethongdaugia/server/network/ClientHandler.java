package com.javfxtutorial.hethongdaugia.server.network;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            System.out.println("Luong " + this.getName() + " dang chay");
            ClientHandlerContextHolder.set(this);
            System.out.println("Vua khoi tao " + ClientHandlerContextHolder.get().getName());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Command cmd = (Command) in.readObject();
                Response rp;
                try {
                    rp = cmd.handle();
                } catch (Exception e) {
                    // Bất kỳ RuntimeException nào cũng không làm chết server
                    System.err.println("[SERVER ERROR] Command " + cmd.getClass().getSimpleName() + " crash: " + e.getMessage());
                    e.printStackTrace();
                    rp = new Response(false, "Lỗi server: " + e.getMessage(), null, cmd);
                }
                if (rp != null) {
                    synchronized (out) {
                        out.writeObject(rp);
                        out.flush();
                        out.reset();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client ngung ket noi");
            try {
                clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            ClientHandlerContextHolder.clear();
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Client ngat ket noi");
                }
            }
        }
    }

    @Override
    public void onPlaceBid(BidTransaction bid, ClientHandler senderThread) {
        if (senderThread == this || out == null) {
            return;
        }

        Response rp = new Response(true, "Co nguoi moi dat gia", bid, new PlaceBidCommand());
        try {
            synchronized (out) {
                out.writeObject(rp);
                out.flush();
                out.reset();
            }
            System.out.println("Da gui PlaceBidCommand ve cho luong " + this.getName());
        } catch (IOException e) {
            System.out.println("Loi outputStream");
        }
    }
}
