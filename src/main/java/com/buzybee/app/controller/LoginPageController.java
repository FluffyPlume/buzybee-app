package com.buzybee.app.controller;

import com.buzybee.app.MainApp;
import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.UserDAO;
import com.buzybee.app.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginPageController {
    // --- ANIMATION CONSTANTS ---
    private static final Duration SLIDE_DURATION = Duration.millis(650);
    private static final double OVERLAY_TRAVEL = -332;
    private static final double OVERLAY_WIDTH = 350;
    private static final double FORM_OFFSET = 36;
    private boolean signUpMode;
    private boolean animating;

    // --- UI ELEMENTS ---
    @FXML private AnchorPane brandPanel;
    @FXML private AnchorPane signInForm;
    @FXML private AnchorPane signUpForm;
    @FXML private ImageView beeImage;
    @FXML private Label brandTitle;
    @FXML private Label brandSubtitle;
    @FXML private Label brandPrompt;
    @FXML private Button brandSwitchButton;
    @FXML private Label signInStatusLabel;
    @FXML private Label signUpStatusLabel;

    // --- YOUR BACKEND TOOLS ---
    private final UserDAO userDAO = new UserDAO();

    // Sign-In Fields
    @FXML private TextField signInIdentifierField;
    @FXML private PasswordField signInPasswordField;

    // Sign-Up Fields (NEW)
    @FXML private TextField signUpEmailField;
    @FXML private TextField signUpUsernameField;
    @FXML private PasswordField signUpPasswordField;

    @FXML
    private void initialize() {
        submitOnEnter(signInIdentifierField, this::handleSignIn);
        submitOnEnter(signInPasswordField, this::handleSignIn);
        submitOnEnter(signUpEmailField, this::handleSignUpSubmit);
        submitOnEnter(signUpUsernameField, this::handleSignUpSubmit);
        submitOnEnter(signUpPasswordField, this::handleSignUpSubmit);

        if (signUpForm != null) {
            signUpForm.setDisable(true);
            signUpForm.setMouseTransparent(true);
        }
    }

    private void submitOnEnter(TextField field, Runnable submitAction) {
        if (field != null) {
            field.setOnAction(event -> submitAction.run());
        }
    }

    // ==========================================
    // BACKEND LOGIC
    // ==========================================

    @FXML
    private void handleSignIn() {
        String identifier = signInIdentifierField.getText();
        String password = signInPasswordField.getText();

        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            signInStatusLabel.setText("Please enter your credentials.");
            return;
        }

        User loggedInUser = userDAO.authenticate(identifier, password);
        if (loggedInUser == null) {
            signInStatusLabel.setText("Invalid username/email or password");
            return;
        }

        signInStatusLabel.setText("Success! Loading workspace...");
        SessionManager.getInstance().startSession(loggedInUser);
        showDashboard();
    }

    @FXML
    private void handleSignUpSubmit() {
        String email = signUpEmailField.getText();
        String user = signUpUsernameField.getText();
        String pass = signUpPasswordField.getText();

        if (email == null || email.isBlank() || user == null || user.isBlank() || pass == null || pass.isBlank()) {
            signUpStatusLabel.setText("Please fill out all fields.");
            return;
        }

        boolean isRegistered = userDAO.registerUser(user, pass, email);

        if (isRegistered) {
            signUpStatusLabel.setText("Success! Creating your workspace...");
            User newUser = new User(user, pass, email);
            SessionManager.getInstance().startSession(newUser);
            showDashboard();
        } else {
            signUpStatusLabel.setText("Username or email already taken.");
        }
    }

    private void showDashboard() {
        loadScreen("fxml/app-shell.fxml", 1280, 720);
    }

    private void loadScreen(String fxmlPath, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) signInForm.getScene().getWindow();

            stage.setScene(new Scene(root, width, height));
            stage.setResizable(true);
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException exception) {
            signInStatusLabel.setText("Screen could not be loaded.");
            signUpStatusLabel.setText("Screen could not be loaded.");
            exception.printStackTrace();
        }
    }

    // ==========================================
    // DUMMY BUTTONS & ANIMATION LOGIC
    // ==========================================

    @FXML
    private void handleGoogleSignIn() {
        signInStatusLabel.setText("Google Sign In is not supported.");
    }

    @FXML
    private void handleForgotPassword() {
        signInStatusLabel.setText("Password recovery coming soon.");
    }

    @FXML
    private void handleGoogleSignUp() {
        signUpStatusLabel.setText("Google Sign Up is not supported.");
    }

    @FXML
    private void showSignUp() {
        animateMode(true);
    }

    @FXML
    private void showSignIn() {
        animateMode(false);
    }

    private void animateMode(boolean targetSignUpMode) {
        if (animating || signUpMode == targetSignUpMode) { return; }

        animating = true;
        signUpMode = targetSignUpMode;
        prepareForms(targetSignUpMode);
        brandPanel.setPrefWidth(OVERLAY_WIDTH);
        updatePanelSide(null);

        ParallelTransition transition = new ParallelTransition(
                slide(brandPanel, targetSignUpMode ? OVERLAY_TRAVEL : 0),
                slide(signInForm, targetSignUpMode ? -FORM_OFFSET : 0),
                fade(signInForm, targetSignUpMode ? 0 : 1),
                slide(signUpForm, targetSignUpMode ? 0 : FORM_OFFSET),
                fade(signUpForm, targetSignUpMode ? 1 : 0),
                fade(beeImage, 0),
                flutter(beeImage, targetSignUpMode ? -8 : 8),
                bob(beeImage, targetSignUpMode ? -10 : 10),
                fade(brandTitle, 0),
                fade(brandSubtitle, 0),
                fade(brandPrompt, 0),
                fade(brandSwitchButton, 0)
        );

        transition.setOnFinished(event -> {
            signInForm.setDisable(targetSignUpMode);
            signInForm.setMouseTransparent(targetSignUpMode);
            signUpForm.setDisable(!targetSignUpMode);
            signUpForm.setMouseTransparent(!targetSignUpMode);
            updateBrandCopy(targetSignUpMode);
            fadeBrandContentIn();
        });
        transition.play();
    }

    private void prepareForms(boolean targetSignUpMode) {
        signInForm.setDisable(false);
        signInForm.setMouseTransparent(false);
        signUpForm.setDisable(false);
        signUpForm.setMouseTransparent(false);
        brandSwitchButton.setDisable(true);
        brandPanel.toFront();
        beeImage.toFront();

        if (targetSignUpMode) {
            signUpForm.setTranslateX(FORM_OFFSET);
            signUpForm.setOpacity(0);
        } else {
            signInForm.setTranslateX(-FORM_OFFSET);
            signInForm.setOpacity(0);
        }
    }

    private void updateBrandCopy(boolean targetSignUpMode) {
        if (targetSignUpMode) {
            setStyleClass(brandTitle, "welcome-title");
            setStyleClass(brandSubtitle, "welcome-brand-title");
            setStyleClass(brandPrompt, "signin-prompt");
            setStyleClass(brandSwitchButton, "signin-switch-button");
            brandPanel.setPrefWidth(OVERLAY_WIDTH);
            brandTitle.setLayoutX(0); brandTitle.setLayoutY(100);
            brandTitle.setPrefWidth(OVERLAY_WIDTH);
            brandTitle.setAlignment(javafx.geometry.Pos.CENTER);
            brandTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            brandSubtitle.setLayoutX(0); brandSubtitle.setLayoutY(116);
            brandSubtitle.setPrefWidth(OVERLAY_WIDTH);
            brandSubtitle.setAlignment(javafx.geometry.Pos.CENTER);
            brandSubtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            brandPrompt.setLayoutX(0); brandPrompt.setLayoutY(239);
            brandPrompt.setPrefWidth(OVERLAY_WIDTH);
            brandSwitchButton.setLayoutX(76); brandSwitchButton.setLayoutY(270);
            brandSwitchButton.setPrefWidth(198); brandSwitchButton.setPrefHeight(45);
            brandSwitchButton.setAlignment(javafx.geometry.Pos.CENTER);
            brandSwitchButton.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            beeImage.setFitHeight(142); beeImage.setFitWidth(175);
            beeImage.setLayoutX(257); beeImage.setLayoutY(1);
            beeImage.setScaleX(-1); beeImage.setTranslateY(-8);
            brandTitle.setText("WELCOME TO");
            brandSubtitle.setText("BuzyBee!");
            brandPrompt.setText("Sign In to enjoy the app!");
            brandSwitchButton.setText("Sign In!");
            brandSwitchButton.setOnAction(event -> showSignIn());
        } else {
            setStyleClass(brandTitle, "brand-title");
            setStyleClass(brandSubtitle, "brand-subtitle");
            setStyleClass(brandPrompt, "signup-prompt");
            setStyleClass(brandSwitchButton, "signup-button");
            brandPanel.setPrefWidth(OVERLAY_WIDTH);
            brandTitle.setLayoutX(0); brandTitle.setLayoutY(38);
            brandTitle.setPrefWidth(OVERLAY_WIDTH);
            brandTitle.setAlignment(javafx.geometry.Pos.CENTER);
            brandTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            brandSubtitle.setLayoutX(54); brandSubtitle.setLayoutY(140);
            brandSubtitle.setPrefWidth(240);
            brandPrompt.setLayoutX(0); brandPrompt.setLayoutY(223);
            brandPrompt.setPrefWidth(OVERLAY_WIDTH);
            brandSwitchButton.setLayoutX(87.5); brandSwitchButton.setLayoutY(254);
            brandSwitchButton.setPrefWidth(175); brandSwitchButton.setPrefHeight(44);
            brandSwitchButton.setAlignment(javafx.geometry.Pos.CENTER);
            brandSwitchButton.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            beeImage.setFitHeight(165); beeImage.setFitWidth(203);
            beeImage.setLayoutX(260); beeImage.setLayoutY(218);
            beeImage.setScaleX(1); beeImage.setTranslateY(8);
            brandTitle.setText("BuzyBee");
            brandSubtitle.setText("Buzz through your goals,\none session at a time.");
            brandPrompt.setText("Don't have an account?");
            brandSwitchButton.setText("Sign Up!");
            brandSwitchButton.setOnAction(event -> showSignUp());
        }
        updatePanelSide(targetSignUpMode);
    }

    private void setStyleClass(Node node, String styleClass) {
        node.getStyleClass().setAll(styleClass);
    }

    private void updatePanelSide(Boolean leftSide) {
        brandPanel.getStyleClass().removeAll("brand-panel", "brand-panel-left", "brand-panel-moving");
        if (leftSide == null) {
            brandPanel.getStyleClass().add("brand-panel-moving");
        } else {
            brandPanel.getStyleClass().add(leftSide ? "brand-panel-left" : "brand-panel");
        }
    }

    private TranslateTransition slide(Node node, double destinationX) {
        TranslateTransition transition = new TranslateTransition(SLIDE_DURATION, node);
        transition.setToX(destinationX);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private FadeTransition fade(Node node, double destinationOpacity) {
        FadeTransition transition = new FadeTransition(SLIDE_DURATION, node);
        transition.setToValue(destinationOpacity);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private void fadeBrandContentIn() {
        ParallelTransition transition = new ParallelTransition(
                quickFade(brandTitle, 1),
                quickFade(brandSubtitle, 1),
                quickFade(brandPrompt, 1),
                quickFade(brandSwitchButton, 1),
                quickFade(beeImage, 1),
                quickFlutter(beeImage),
                quickBob(beeImage)
        );
        transition.setOnFinished(event -> {
            brandSwitchButton.setDisable(false);
            animating = false;
        });
        transition.play();
    }

    private FadeTransition quickFade(Node node, double destinationOpacity) {
        FadeTransition transition = new FadeTransition(Duration.millis(180), node);
        transition.setToValue(destinationOpacity);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private RotateTransition flutter(Node node, double destinationAngle) {
        RotateTransition transition = new RotateTransition(SLIDE_DURATION, node);
        transition.setToAngle(destinationAngle);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private TranslateTransition bob(Node node, double destinationY) {
        TranslateTransition transition = new TranslateTransition(SLIDE_DURATION, node);
        transition.setToY(destinationY);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private RotateTransition quickFlutter(Node node) {
        RotateTransition transition = new RotateTransition(Duration.millis(180), node);
        transition.setToAngle(0);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private TranslateTransition quickBob(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(180), node);
        transition.setToY(0);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }
}
