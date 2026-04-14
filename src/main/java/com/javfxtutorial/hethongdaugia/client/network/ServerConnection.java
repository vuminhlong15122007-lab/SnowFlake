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

    public ServerConnection(){
        //khởi tạo socket connect tới server và luông để truyền/ nhận dữ liệu
        try {
            this.clientSocket = new Socket(IP, PORT);
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Không kết nối được server");
        }

    }

    public ObjectOutputStream getOut() {
        return out;
    }
    public ObjectInputStream getIn() {
        return in;
    }

    // Gửi command và chờ response (đồng bộ)
    public Response sendCommand(Command cmd) {
        try {
            out.writeObject(cmd);
            out.flush();
            return (Response) in.readObject();  // nhận vể response của server
        } catch (Exception e) {
            return new Response(false, "Lỗi kết nối", null);
        }
    }
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (clientSocket != null) {
            this.clientSocket.close();
        }
    }
}
