package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SignInController {
    @FXML
    private Label statusLabel;

    @FXML
    private void handleSignIn() {
        statusLabel.setText("Sign in is ready to connect.");
    }

    @FXML
    private void handleGoogleSignIn() {
        statusLabel.setText("Google sign in is ready to connect.");
    }

    @FXML
    private void handleSignUp() {
        statusLabel.setText("Sign up is ready to connect.");
    }

    @FXML
    private void handleForgotPassword() {
        statusLabel.setText("Password recovery is ready to connect.");
    }
}
