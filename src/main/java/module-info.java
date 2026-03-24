module com.javfxtutorial.hethongdaugia {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.javfxtutorial.hethongdaugia to javafx.fxml;
    exports com.javfxtutorial.hethongdaugia;
}