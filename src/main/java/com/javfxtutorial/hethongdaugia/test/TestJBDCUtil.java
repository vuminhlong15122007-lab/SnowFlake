package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DatabaseConnectionException;
import com.javfxtutorial.hethongdaugia.server.dao.JDBCUtil;

import java.sql.Connection;

public class TestJBDCUtil {
    static void main() throws DatabaseConnectionException {
        Connection connection = JDBCUtil.getConnection();
        System.out.println(connection);
        JDBCUtil.closeConnection(connection);
    }
}
