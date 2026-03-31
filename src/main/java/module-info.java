
module com.javfxtutorial.hethongdaugia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.jdi;
    requires socket.io.client;
    requires org.json;


    opens com.javfxtutorial.hethongdaugia to javafx.fxml;
    opens com.javfxtutorial.hethongdaugia.controller to javafx.fxml;

    exports com.javfxtutorial.hethongdaugia;
}