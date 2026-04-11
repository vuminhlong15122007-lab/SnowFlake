package com.javfxtutorial.hethongdaugia.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String host = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
            String port = "4000";
            // Cẩn thận chỗ này: nếu bạn đã tạo DB riêng trên HeidiSQL thì sửa lại tên
            String database = "test";
            String username = "3sSzrSFdZfqFKd5.root";
            String password = "8J1D7oKnbj8npKF1";

            // Chuỗi URL chuẩn cho TiDB Cloud
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?enabledTLSProtocols=TLSv1.2,TLSv1.3&sslMode=REQUIRED"; // Đổi thành REQUIRED cho "nhẹ"

            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Kết nối thành công tới TiDB Cloud!");

        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Thiếu Driver MySQL!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
            // Nếu lỗi "Access denied for IP", hãy kiểm tra lại IP Access List trên web TiDB
        }
        return connection;
    }

    public static void closeConnection(Connection connection){
        try{
            if (connection != null && !connection.isClosed()){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}