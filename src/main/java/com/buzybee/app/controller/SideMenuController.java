package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SideMenuController {
    @FXML private Button btnDashboard;
    @FXML private Button btnCalendar;
    @FXML private Button btnTasks;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;

    private AppShellController appShellController;

    void setAppShellController(AppShellController appShellController) {
        this.appShellController = appShellController;
    }

    // Highlight helpers — callable from AppShellController so navigation triggered
    // from inside a page (e.g. the dashboard's "Open Tasks" button) keeps the rail in sync.
    void selectDashboard() {
        setActiveNav(btnDashboard);
    }

    void selectCalendar() {
        setActiveNav(btnCalendar);
    }

    void selectTasks() {
        setActiveNav(btnTasks);
    }

    void selectAnalytics() {
        setActiveNav(btnAnalytics);
    }

    void selectSettings() {
        setActiveNav(btnSettings);
    }

    @FXML
    private void onNavDashboard() {
        appShellController.showDashboardHome();
    }

    @FXML
    private void onNavCalendar() {
        appShellController.showCalendar();
    }

    @FXML
    private void onNavTasks() {
        appShellController.showTasks();
    }

    @FXML
    private void onNavAnalytics() {
        appShellController.showAnalytics();
    }

    @FXML
    private void onNavSettings() {
        appShellController.showSettings();
    }

    @FXML
    private void onNavLogout() {
        setActiveNav(btnLogout);
        appShellController.showLogout();
    }

    private void setActiveNav(Button selected) {
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
}
