package com.buzybee.app.controller;

import com.buzybee.app.MainApp;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SignUpController {
    @FXML
    private AnchorPane brandPanel;

    @FXML
    private AnchorPane formPane;

    @FXML
    private ImageView beeImage;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleSignUp() {
        statusLabel.setText("Sign up is ready to connect.");
    }

    @FXML
    private void handleGoogleSignUp() {
        statusLabel.setText("Google sign up is ready to connect.");
    }

    @FXML
    private void handleSignIn() {
        playPanelTransition("fxml/sign-in.fxml", 332, 360, -360);
    }

    private void playPanelTransition(String nextScreen, double panelDestinationX, double currentFormDestinationX,
                                     double nextFormStartX) {
        brandPanel.setDisable(true);
        formPane.setDisable(true);

        try {
            Parent nextRoot = loadNextRoot(nextScreen);
            Node nextFormPane = detachNode(nextRoot.lookup("#formPane"));
            Node nextBeeImage = detachNode(nextRoot.lookup("#beeImage"));
            Pane currentRoot = (Pane) statusLabel.getScene().getRoot();

            nextFormPane.setTranslateX(nextFormStartX);
            nextBeeImage.setTranslateX(nextFormStartX);
            int panelIndex = currentRoot.getChildren().indexOf(brandPanel);
            currentRoot.getChildren().add(panelIndex, nextFormPane);
            currentRoot.getChildren().add(panelIndex + 1, nextBeeImage);
            currentRoot.getStylesheets().addAll(nextRoot.getStylesheets());

            ParallelTransition slide = new ParallelTransition(
                    createSlide(brandPanel, panelDestinationX),
                    createSlide(formPane, currentFormDestinationX),
                    createSlide(beeImage, currentFormDestinationX),
                    createSlide(nextFormPane, 0),
                    createSlide(nextBeeImage, 0)
            );
            slide.setOnFinished(event -> loadScreen(nextScreen));
            slide.play();
        } catch (IOException exception) {
            brandPanel.setDisable(false);
            formPane.setDisable(false);
            statusLabel.setText("Could not open sign in.");
        }
    }

    private Parent loadNextRoot(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        return loader.load();
    }

    private Node detachNode(Node node) {
        Pane parent = (Pane) node.getParent();
        parent.getChildren().remove(node);
        return node;
    }

    private TranslateTransition createSlide(Node node, double destinationX) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(650), node);
        slide.setToX(destinationX);
        slide.setInterpolator(Interpolator.EASE_BOTH);
        return slide;
    }

    private void loadScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 682, 383));
        } catch (IOException exception) {
            brandPanel.setDisable(false);
            formPane.setDisable(false);
            statusLabel.setText("Could not open sign in.");
        }
    }
}
