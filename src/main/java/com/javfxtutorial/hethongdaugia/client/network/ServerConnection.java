package com.javfxtutorial.hethongdaugia.client.network;

import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConnection {
    private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);

    private static final String DEFAULT_IP = "10.11.20.248";
    private static final int DEFAULT_PORT = 5000;

    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private static volatile ServerConnection instance;

    public static ServerConnection getInstance() throws IOException {
        ServerConnection local = instance;
        if (local == null || !local.isConnected()) {
            synchronized (ServerConnection.class) {
                local = instance;
                if (local == null || !local.isConnected()) {
                    if (local != null) {
                        local.close();
                    }
                    instance = local = new ServerConnection();
                }
            }
        }
        return local;
    }

    private ServerConnection() throws IOException {
        String ip = System.getProperty("snowflake.server.host", DEFAULT_IP);
        int port = Integer.getInteger("snowflake.server.port", DEFAULT_PORT);

        this.clientSocket = new Socket(ip, port);
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public synchronized void sendCommand(Command cmd) throws SendFailedException {
        try {
            out.writeObject(cmd);
            out.flush();
            out.reset();
        } catch (IOException e) {
            close();
            log.error("Gửi command thất bại: {}", e.getMessage(), e);
            throw new SendFailedException(cmd.getClass().getSimpleName());
        }
    }

    public Response receiveResponse() throws IOException, ClassNotFoundException {
        return (Response) in.readObject();
    }

    public void close() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            log.error("Lỗi khi đóng socket: {}", e.getMessage());
        } finally {
            instance = null;
        }
    }

    public boolean isConnected() {
        return clientSocket != null
                && clientSocket.isConnected()
                && !clientSocket.isClosed()
                && !clientSocket.isInputShutdown()
                && !clientSocket.isOutputShutdown();
    }
}
