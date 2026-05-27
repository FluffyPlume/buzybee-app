package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardController {
    @FXML private Button btnDashboard;
    @FXML private Button btnCalendar;
    @FXML private Button btnTasks;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;

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

    private void setActiveNav(Button selected) {
        Button[] buttons = {btnDashboard, btnCalendar, btnTasks, btnAnalytics, btnSettings};
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
}
