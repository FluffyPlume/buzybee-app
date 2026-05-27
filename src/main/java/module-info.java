module com.buzybee.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires java.sql;

    opens com.buzybee.app to javafx.fxml;
    opens com.buzybee.app.model to com.google.gson;
    opens com.buzybee.app.controller to javafx.fxml;
    exports com.buzybee.app;
}
