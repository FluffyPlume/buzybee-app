package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class DashboardController {

    //  UI Buttons
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

    // The Content Area
    @FXML private StackPane contentArea;

    @FXML
    private void initialize() {
        // Set the default active button and load the default screen
        setActiveNav(btnDashboard);
        // loadScreen("dashboard-home.fxml"); // Uncomment this once you make the file
    }

    // Navigation Click Events

    @FXML
    private void onNavDashboard() {
        setActiveNav(btnDashboard);
        // loadScreen("dashboard-home.fxml");
    }

    @FXML
    private void onNavCalendar() {
        setActiveNav(btnCalendar);
        // loadScreen("calendar.fxml"); // Uncomment this once you make the file
    }

    @FXML
    private void onNavTasks() {
        setActiveNav(btnTasks);
        // loadScreen("tasks.fxml"); // Uncomment this once you make the file
    }

    @FXML
    private void onNavAnalytics() {
        setActiveNav(btnAnalytics);
        // loadScreen("analytics.fxml");
    }

    @FXML
    private void onNavSettings() {
        setActiveNav(btnSettings);
        // loadScreen("settings.fxml");
    }

    @FXML
    private void onNavLogout() {
        setActiveNav(btnLogout);
        SessionManager.getInstance().endSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/auth.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) btnLogout.getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setWidth(682);
            stage.setHeight(383);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.out.println("Could not load the login screen!");
            e.printStackTrace();
        }
    }

    // Screen Swap dun sa contentArea
    private void loadScreen(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/" + fxmlFileName));
            Node newScreen = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newScreen);
        } catch (Exception e) {
            System.out.println("Could not load the screen: " + fxmlFileName);
            e.printStackTrace();
        }
    }



    @FXML
    private void onDarkMode() { setActiveThemeButton(btnDarkMode); }

    @FXML
    private void onLightMode() { setActiveThemeButton(btnLightMode); }

    @FXML
    private void onTopNotification() { setActiveTopButton(btnNotification); }

    @FXML
    private void onTopUser() { setActiveTopButton(btnUser); }

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