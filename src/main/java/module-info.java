module com.javfxtutorial.hethongdaugia {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.sql;
  requires mysql.connector.j;
  requires com.zaxxer.hikari;
  requires jdk.jdi;
  requires org.json;
  requires java.desktop;
  requires jdk.compiler;
  requires org.slf4j;
  requires java.prefs;

  opens com.javfxtutorial.hethongdaugia.client to
      javafx.fxml;
  opens com.javfxtutorial.hethongdaugia.client.controller to
      javafx.fxml;
  opens com.javfxtutorial.hethongdaugia.view.fxml to
      javafx.fxml;
  opens com.javfxtutorial.hethongdaugia.common.model.domain to
      javafx.base;

  exports com.javfxtutorial.hethongdaugia.client;

  opens com.javfxtutorial.hethongdaugia.client.Util to
      javafx.base,
      javafx.fxml;
}
