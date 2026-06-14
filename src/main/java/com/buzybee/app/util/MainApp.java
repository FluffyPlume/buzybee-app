package com.buzybee.app.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Font.loadFont(MainApp.class.getResource("fonts/Ramsina-Regular.ttf").toExternalForm(), 16);
        Font.loadFont(MainApp.class.getResource("fonts/Fredoka-SemiBold.ttf").toExternalForm(), 16);
        Font.loadFont(MainApp.class.getResource("fonts/Nunito-Regular.ttf").toExternalForm(), 16);
        Font.loadFont(MainApp.class.getResource("fonts/Nunito-SemiBold.ttf").toExternalForm(), 16);

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("fxml/auth.fxml"));
        Scene scene = new Scene(loader.load(), 682, 383);


        Image appIcon = new Image(getClass().getResourceAsStream("/com/buzybee/app/images/Bee.png"));
        stage.getIcons().add(appIcon);
        stage.setTitle("BuzyBee");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
