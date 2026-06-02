package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class TopBarController {
    @FXML private Button btnDarkMode;
    @FXML private Button btnLightMode;
    @FXML private Button btnNotification;
    @FXML private Button btnUser;

    private AppShellController appShellController;

    @FXML
    private void initialize() {
        setActiveThemeButton(btnLightMode);
    }

    void setAppShellController(AppShellController appShellController) {
        this.appShellController = appShellController;
    }

    @FXML
    private void onDarkMode() {
        setActiveThemeButton(btnDarkMode);
        appShellController.setDarkMode(true);
    }

    @FXML
    private void onLightMode() {
        setActiveThemeButton(btnLightMode);
        appShellController.setDarkMode(false);
    }

    @FXML
    private void onTopNotification() {
        setActiveTopButton(btnNotification);
    }

    @FXML
    private void onTopUser() {
        setActiveTopButton(btnUser);
    }

    void clearTopSelection() {
        Button[] buttons = {btnNotification, btnUser};
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("top-action-btn-active");
            }
        }
    }

    boolean isTopActionTarget(Node target) {
        return isInside(target, btnNotification) || isInside(target, btnUser);
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

    private boolean isInside(Node target, Node parent) {
        while (target != null) {
            if (target == parent) {
                return true;
            }
            target = target.getParent();
        }
        return false;
    }
}
