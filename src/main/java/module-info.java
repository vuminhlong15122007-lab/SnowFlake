module com.javfxtutorial.hethongdaugia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.javfxtutorial.hethongdaugia to javafx.fxml;
    exports com.javfxtutorial.hethongdaugia;
}