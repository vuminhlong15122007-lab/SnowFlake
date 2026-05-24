package com.javfxtutorial.hethongdaugia.server.network;

import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends Thread implements BidListener {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User currentUser;

    private static final CopyOnWriteArrayList<ClientHandler> allClients =
            new CopyOnWriteArrayList<>();

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        // ObjectOutputStream phải khởi tạo TRƯỚC ObjectInputStream
        if (socket != null) {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        }
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    @Override
    public void run() {
        try {
            allClients.add(this);
            log.info("Luong {} dang chay", this.getName());
            ClientHandlerContextHolder.set(this);
            log.info("Vua khoi tao {}", ClientHandlerContextHolder.get().getName());

            while (true) {
                Object obj = in.readObject();

                if (!(obj instanceof Command cmd)) {
                    log.warn("[SERVER WARN] Object khong phai Command: {}", obj.getClass().getName());
                    continue;
                }

                if (!isAllowedCommand(cmd)) {
                    log.warn("[SERVER WARN] Command bi tu choi: {}", cmd.getClass().getName());
                    continue;
                }

                Response rp;
                try {
                    rp = cmd.handle();
                } catch (Exception e) {
                    log.error("[SERVER ERROR] Command {} crash: {}",
                            cmd.getClass().getSimpleName(), e.getMessage(), e);
                    rp = new Response(false, "Lỗi server: " + e.getMessage(), null, cmd);
                }

                if (rp != null) {
                    sendResponse(rp);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.info("Client ngung ket noi");
        } finally {
            AuctionManager.getInstance().unregisterListenerFromAll(this);
            ClientHandlerContextHolder.clear();
            allClients.remove(this);
            try { if (clientSocket != null) clientSocket.close(); }
            catch (IOException e) { log.info("Loi dong socket: {}", e.getMessage()); }
        }
    }

    private void sendResponse(Response rp) {
        try {
            synchronized (out) {
                out.writeObject(rp);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            log.error("Loi gui response: {}", e.getMessage());
        }
    }

    @Override
    public void onPlaceBid(BidTransaction bid, ClientHandler senderThread) {
        if (senderThread == this) return;
        Response rp = new Response(true, "Co nguoi moi dat gia", bid, new PlaceBidCommand());
        sendResponse(rp);
        log.info("Da gui PlaceBidCommand ve cho luong {}", this.getName());
    }

    public static void broadcast(Response rp) {
        for (ClientHandler ch : allClients) {
            ch.sendResponse(rp);
        }
    }

    public static void broadcastToSeller(int sellerId, Response rp) {
        for (ClientHandler ch : allClients) {
            if (ch.currentUser != null && ch.currentUser.getId() == sellerId) {
                ch.sendResponse(rp);
                log.info("Da gui thong bao toi seller id={}", sellerId);
                return;
            }
        }
        log.warn("Seller id={} khong online, bo qua thong bao", sellerId);
    }

    private boolean isAllowedCommand(Command cmd) {
        return cmd.getClass().getPackageName()
                .equals("com.javfxtutorial.hethongdaugia.common.model.Command");
    }
    public static void broadcastExcept(int excludeUserId, Response rp) {
        for (ClientHandler ch : allClients) {
            if (ch.currentUser != null && ch.currentUser.getId() != excludeUserId) {
                ch.sendResponse(rp);
            }
        }
        log.info("Da gui broadcast (ngoai tru user id={}) xong", excludeUserId);
    }
}