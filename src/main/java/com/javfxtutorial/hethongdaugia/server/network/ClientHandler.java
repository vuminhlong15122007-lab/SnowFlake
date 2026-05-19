package com.javfxtutorial.hethongdaugia.server.network;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import java.io.ObjectInputFilter;

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

            in.setObjectInputFilter(this::validateInput);

            while (true) {
                Object obj = in.readObject();

                if (!(obj instanceof Command cmd)) {
                    System.err.println("[SERVER WARN] Object khong phai Command: " + obj.getClass().getName());
                    continue;
                }

                if (!isAllowedCommand(cmd)) {
                    System.err.println("[SERVER WARN] Command bi tu choi: " + cmd.getClass().getName());
                    continue;
                }

                Response rp;
                try {
                    rp = cmd.handle();
                } catch (Exception e) {
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
        } finally {
            AuctionManager.getInstance().unregisterListenerFromAll(this);
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
    // 2 method này sinh ra để fix bug B08 => kiểm tra trước
    private boolean isAllowedCommand(Command cmd) {
        return cmd.getClass().getPackageName()
                .equals("com.javfxtutorial.hethongdaugia.common.model.Command");
    }
    private ObjectInputFilter.Status validateInput(ObjectInputFilter.FilterInfo info) {
        Class<?> clazz = info.serialClass();

        if (info.depth() > 20) return ObjectInputFilter.Status.REJECTED;
        if (info.references() > 10_000) return ObjectInputFilter.Status.REJECTED;
        if (info.arrayLength() >= 0 && info.arrayLength() > 1_000_000) {
            return ObjectInputFilter.Status.REJECTED;
        }

        if (clazz == null) return ObjectInputFilter.Status.UNDECIDED;
        if (clazz.isArray()) return ObjectInputFilter.Status.UNDECIDED;
        if (clazz.isPrimitive()) return ObjectInputFilter.Status.ALLOWED;

        if (Command.class.isAssignableFrom(clazz)) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        String name = clazz.getName();

        if (name.startsWith("com.javfxtutorial.hethongdaugia.common.model.")
                || name.startsWith("com.javfxtutorial.hethongdaugia.common.network.")
                || name.startsWith("java.lang.")
                || name.startsWith("java.util.")
                || name.startsWith("java.math.")
                || name.startsWith("java.time.")) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        return ObjectInputFilter.Status.REJECTED;
    }
}
