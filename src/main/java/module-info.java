
module com.javfxtutorial.hethongdaugia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.jdi;
    requires socket.io.client;
    requires org.json;
    requires java.desktop;
    requires jdk.compiler;


    opens com.javfxtutorial.hethongdaugia to javafx.fxml;
    opens com.javfxtutorial.hethongdaugia.client.controller to javafx.fxml;

    exports com.javfxtutorial.hethongdaugia;
    exports com.javfxtutorial.hethongdaugia.client;
    opens com.javfxtutorial.hethongdaugia.client to javafx.fxml;
}