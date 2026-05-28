package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardController {
    @FXML private Button btnDashboard;
    @FXML private Button btnCalendar;
    @FXML private Button btnTasks;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;
    @FXML private Button btnDarkMode;
    @FXML private Button btnLightMode;
    @FXML private Button btnNotification;
    @FXML private Button btnUser;

    @FXML
    private void initialize() {
        setActiveNav(btnDashboard);
    }

    @FXML
    private void onNavDashboard() {
        setActiveNav(btnDashboard);
    }

    @FXML
    private void onNavCalendar() {
        setActiveNav(btnCalendar);
    }

    @FXML
    private void onNavTasks() {
        setActiveNav(btnTasks);
    }

    @FXML
    private void onNavAnalytics() {
        setActiveNav(btnAnalytics);
    }

    @FXML
    private void onNavSettings() {
        setActiveNav(btnSettings);
    }

    @FXML
    private void onNavLogout() {
        setActiveNav(btnLogout);
    }

    @FXML
    private void onDarkMode() {
        setActiveThemeButton(btnDarkMode);
    }

    @FXML
    private void onLightMode() {
        setActiveThemeButton(btnLightMode);
    }

    @FXML
    private void onTopNotification() {
        setActiveTopButton(btnNotification);
    }

    @FXML
    private void onTopUser() {
        setActiveTopButton(btnUser);
    }

    private void setActiveNav(Button selected) {
        clearTopSelection();

        Button[] buttons = {btnDashboard, btnCalendar, btnTasks, btnAnalytics, btnSettings, btnLogout};
        for (Button button : buttons) {
            if (!button.getStyleClass().contains("rail-btn")) {
                button.getStyleClass().add("rail-btn");
            }

            button.getStyleClass().remove("rail-btn-active");
            if (button == selected) {
                button.getStyleClass().add("rail-btn-active");
            }
        }
    }

    @FXML
    private void clearTopSelection() {
        Button[] buttons = {btnNotification, btnUser};
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("top-action-btn-active");
            }
        }
    }

    private void setActiveTopButton(Button selected) {
        Button[] buttons = {btnNotification, btnUser};
        for (Button button : buttons) {
            if (!button.getStyleClass().contains("top-action-btn")) {
                button.getStyleClass().add("top-action-btn");
            }

            button.getStyleClass().remove("top-action-btn-active");
            if (button == selected) {
                button.getStyleClass().add("top-action-btn-active");
            }
        }
    }

    private void setActiveThemeButton(Button selected) {
        Button[] buttons = {btnDarkMode, btnLightMode};
        for (Button button : buttons) {
            if (!button.getStyleClass().contains("theme-btn")) {
                button.getStyleClass().add("theme-btn");
            }

            button.getStyleClass().remove("theme-btn-active");
            if (button == selected) {
                button.getStyleClass().add("theme-btn-active");
            }
        }
    }
}
