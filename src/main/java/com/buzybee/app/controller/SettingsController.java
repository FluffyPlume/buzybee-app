package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SettingsController {

    // Account Security
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Notifications
    @FXML private ToggleButton practiceToggle;
    @FXML private ToggleButton dailyToggle;
    @FXML private ToggleButton taskToggle;
    @FXML private Button timePickerButton;

    // FAQ accordion
    @FXML private VBox faqContent;
    @FXML private Label faqChevron;

    // Privacy Policy accordion
    @FXML private VBox privacyContent;
    @FXML private Label privacyChevron;

    // Terms of Service accordion
    @FXML private VBox termsContent;
    @FXML private Label termsChevron;

    // Save button feedback
    @FXML private Button saveButton;

    private AppShellController appShellController;

    // Accordion open/close state
    private boolean faqOpen = false;
    private boolean privacyOpen = false;
    private boolean termsOpen = false;

    // Default reminder time
    private String reminderTime = "8:00 AM";

    @FXML
    private void initialize() {
        // Load current user data into fields
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }

        // Set up toggle switches
        setupSwitch(practiceToggle);
        setupSwitch(dailyToggle);
        setupSwitch(taskToggle);

        // Hide all accordion content by default
        faqContent.setVisible(false);
        faqContent.setManaged(false);
        privacyContent.setVisible(false);
        privacyContent.setManaged(false);
        termsContent.setVisible(false);
        termsContent.setManaged(false);
    }

    void setAppShellController(AppShellController appShellController) {
        this.appShellController = appShellController;
    }

    // ─── Account ────────────────────────────────────────────────────────────────

    @FXML
    private void onSaveChanges() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            String newUsername = usernameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (!newUsername.isEmpty()) {
                currentUser.setUsername(newUsername);
            }
            if (!newEmail.isEmpty()) {
                currentUser.setEmail(newEmail);
            }
            // TODO: call your UserDAO update method here, e.g.:
            // UserDAO.getInstance().updateUser(currentUser);
        }

        // Visual feedback on the save button
        if (saveButton != null) {
            String original = saveButton.getText();
            saveButton.setText("Saved!");
            saveButton.setDisable(true);
            new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                saveButton.setText(original);
                saveButton.setDisable(false);
            })).play();
        }
    }

    @FXML
    private void onChangePassword() {
        String newPassword = passwordField.getText().trim();
        if (newPassword.isEmpty()) {
            passwordField.setPromptText("Password cannot be empty");
            return;
        }
        if (newPassword.length() < 8) {
            passwordField.clear();
            passwordField.setPromptText("Must be at least 8 characters");
            return;
        }

        // TODO: hash and update the password via your UserDAO, e.g.:
        // UserDAO.getInstance().updatePassword(currentUser, newPassword);

        passwordField.clear();
        passwordField.setPromptText("Password updated!");
    }

    // ─── Notifications ──────────────────────────────────────────────────────────

    @FXML
    private void onRestoreDefaults() {
        practiceToggle.setSelected(true);
        dailyToggle.setSelected(true);
        taskToggle.setSelected(false);
        reminderTime = "8:00 AM";
        if (timePickerButton != null) {
            timePickerButton.setText(reminderTime);
        }
    }

    @FXML
    private void onPickTime() {
        // Cycle through common reminder times for simplicity.
        // Replace this with a real TimePicker dialog if your JavaFX version supports it.
        String[] times = {"6:00 AM", "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM",
                "12:00 PM", "3:00 PM", "6:00 PM", "9:00 PM"};
        int currentIndex = 0;
        for (int i = 0; i < times.length; i++) {
            if (times[i].equals(reminderTime)) {
                currentIndex = i;
                break;
            }
        }
        reminderTime = times[(currentIndex + 1) % times.length];
        if (timePickerButton != null) {
            timePickerButton.setText(reminderTime);
        }
    }

    // ─── Accordion (Support and Legal) ──────────────────────────────────────────

    @FXML
    private void onToggleFaq() {
        faqOpen = !faqOpen;
        toggleAccordion(faqContent, faqChevron, faqOpen);
    }

    @FXML
    private void onTogglePrivacy() {
        privacyOpen = !privacyOpen;
        toggleAccordion(privacyContent, privacyChevron, privacyOpen);
    }

    @FXML
    private void onToggleTerms() {
        termsOpen = !termsOpen;
        toggleAccordion(termsContent, termsChevron, termsOpen);
    }

    private void toggleAccordion(VBox content, Label chevron, boolean open) {
        content.setVisible(open);
        content.setManaged(open);
        // Rotate chevron: ⌃ points up when open, down when closed
        chevron.setRotate(open ? 0 : 180);
    }

    // ─── Toggle switch setup ─────────────────────────────────────────────────────

    private void updateSwitchAppearance(ToggleButton toggleButton) {
        HBox track = new HBox();
        track.setAlignment(toggleButton.isSelected() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        track.getStyleClass().add("settings-switch-track");

        Region knob = new Region();
        knob.getStyleClass().add("settings-switch-knob");
        track.getChildren().add(knob);

        toggleButton.setText("");
        toggleButton.setGraphic(track);
        toggleButton.getStyleClass().remove("settings-switch-on");
        toggleButton.getStyleClass().remove("settings-switch-off");
        toggleButton.getStyleClass().add(toggleButton.isSelected() ? "settings-switch-on" : "settings-switch-off");
    }

    private void setupSwitch(ToggleButton toggleButton) {
        updateSwitchAppearance(toggleButton);
        toggleButton.selectedProperty().addListener((observable, oldValue, selected) ->
                updateSwitchAppearance(toggleButton));
    }
}
