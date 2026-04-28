module com.buzybee.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.buzybee.app to javafx.fxml;
    exports com.buzybee.app;
}