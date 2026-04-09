package com.javfxtutorial.hethongdaugia.server.network;


import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.manager.UserManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            Command cmd = (Command) in.readObject();
            Response rp = cmd.handle();
            out.writeObject(rp);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

// logic controller nhaaanj được thao tác ấn vào nút login của user
// -> controller mở connection gửi command đến server -> server check request type của command bằng handle command (pthuc mình tạo để xử lý logic)
// -> request type = login -> server xử lý command login = cách gọi đến usermanager để kiểm tra xem user có tồn tại hay k
// -> usermanager thì lại gọi đến mấy phương thức DAO( data access object - database) để kiểm tra xem có user không
// -> nếu usermanger trả về user thi server trả về respone cho controller (true: trạng thái phthuc - thành công , message - lời nhắn, payload- dữ liệu trả lại)






