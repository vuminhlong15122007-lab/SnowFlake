package com.javfxtutorial.hethongdaugia.server.dao;

import com.mysql.cj.jdbc.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    public static Connection getConnection() {
        Connection connection = null;

        try {
// bước 1: đăng kí MySQL driver với lớp DriverManger
            com.mysql.cj.jdbc.Driver driver = new Driver();//tạo driver
            DriverManager.registerDriver(driver);
// bước 2: khai báo các thông số cơ bản yêu cầu cho kết nối
            String url = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test";
            String username = "3sSzrSFdZfqFKd5.root";
            String password = "8J1D7oKnbj8npKF1";
// bước 3: tạo kết nối
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void closeConnection(Connection connection){
        try{
            if (connection != null){
                connection.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
