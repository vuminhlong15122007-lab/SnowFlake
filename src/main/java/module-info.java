
module com.javfxtutorial.hethongdaugia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.jdi;
    requires org.json;
    requires java.desktop;
    requires jdk.compiler;


    opens com.javfxtutorial.hethongdaugia.client to javafx.fxml;
    opens com.javfxtutorial.hethongdaugia.client.controller to javafx.fxml;
    opens com.javfxtutorial.hethongdaugia.view to javafx.fxml;

    opens com.javfxtutorial.hethongdaugia.common.model to javafx.base;
    opens com.javfxtutorial.hethongdaugia.client.model to javafx.base;


    exports com.javfxtutorial.hethongdaugia.client;
}