package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.database.JBDCUtil;

import java.sql.Connection;

public class TestJBDCUtil {
    static void main() {
        Connection connection = JBDCUtil.getConnection();
        System.out.println(connection);
        JBDCUtil.closeConnection(connection);
    }
}
