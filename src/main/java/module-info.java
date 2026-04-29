module com.buzybee.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;

    opens com.buzybee.app to javafx.fxml;
    opens com.buzybee.app.model to com.google.gson;
    exports com.buzybee.app;
}