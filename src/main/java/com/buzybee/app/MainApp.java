package com.buzybee.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Font.loadFont(MainApp.class.getResource("fonts/Ramsina-Regular.ttf").toExternalForm(), 16);

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("fxml/sign-in.fxml"));
        Scene scene = new Scene(loader.load(), 682, 383);

        stage.setTitle("BuzyBee");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
