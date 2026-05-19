package com.javfxtutorial.hethongdaugia.server.network;

import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.manager.AuctionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler extends Thread implements BidListener {
  private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private static CopyOnWriteArrayList<ClientHandler> allClients = new CopyOnWriteArrayList<>();

  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try {
      allClients.add(this);
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
        allClients.remove(this);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
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

  public static void broadcast(Response rp) {
    allClients.forEach(clientHandler -> {
      try {
        clientHandler.out.writeObject(rp);
        clientHandler.out.flush();
        clientHandler.out.reset();
      } catch (IOException e) {
        log.error("lỗi output stream");
      }

    });
  }
}
