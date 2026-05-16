package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    private static final Logger log = LoggerFactory.getLogger(JDBCUtil.class);

    public static Connection getConnection() throws DatabaseConnectionException {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String host = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
            String port = "4000";
            String database = "test";
            String username = "3sSzrSFdZfqFKd5.root";
            String password = "8J1D7oKnbj8npKF1";

            // Chuỗi URL chuẩn cho TiDB Cloud
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?enabledTLSProtocols=TLSv1.2,TLSv1.3&sslMode=REQUIRED";

            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Kết nối thành công tới TiDB Cloud!");

        } catch (ClassNotFoundException e) {
            log.error("Không tìm thấy MySQL Driver", e);
            throw new DatabaseConnectionException(e);
        } catch (SQLException e) {
            log.error("Lỗi kết nối database: {}", e.getMessage(), e);
            throw new DatabaseConnectionException(e);
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
            log.warn("Lỗi khi đóng kết nối: {}", e.getMessage());
        }
    }
}