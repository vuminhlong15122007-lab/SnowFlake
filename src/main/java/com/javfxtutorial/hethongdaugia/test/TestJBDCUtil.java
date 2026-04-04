package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.server.dao.JBDCUtil;

import java.sql.Connection;

public class TestJBDCUtil {
    static void main() {
        Connection connection = JBDCUtil.getConnection();
        System.out.println(connection);
        JBDCUtil.closeConnection(connection);
    }
}
