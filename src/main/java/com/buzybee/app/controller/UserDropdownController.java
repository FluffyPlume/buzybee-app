package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDropdownController {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;

    void setUserSummary(String username, String email) {
        usernameLabel.setText(username);
        emailLabel.setText(email);
    }
}
