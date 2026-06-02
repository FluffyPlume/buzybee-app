package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

public class TopBarController {
    @FXML private Button btnDarkMode;
    @FXML private Button btnLightMode;
    @FXML private Button btnNotification;
    @FXML private Button btnUser;

    private AppShellController appShellController;
    private Popup notificationDropdown;
    private Parent notificationDropdownContent;
    private NotificationDropdownController notificationDropdownController;
    private Popup userDropdown;
    private Parent userDropdownContent;
    private UserDropdownController userDropdownController;
    private boolean notificationDropdownShowingOnPress;
    private boolean userDropdownShowingOnPress;

    @FXML
    private void initialize() {
        setActiveThemeButton(btnLightMode);
        btnNotification.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
                notificationDropdownShowingOnPress = notificationDropdown != null && notificationDropdown.isShowing());
        btnUser.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
                userDropdownShowingOnPress = userDropdown != null && userDropdown.isShowing());
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
        hideUserDropdown();
        setActiveTopButton(btnNotification);
        toggleNotificationDropdown();
    }

    @FXML
    private void onTopUser() {
        hideNotificationDropdown();
        setActiveTopButton(btnUser);
        toggleUserDropdown();
    }

    void clearTopSelection() {
        hideNotificationDropdown();
        hideUserDropdown();
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

    private void toggleNotificationDropdown() {
        if (notificationDropdownShowingOnPress || (notificationDropdown != null && notificationDropdown.isShowing())) {
            notificationDropdownShowingOnPress = false;
            hideNotificationDropdown();
            btnNotification.getStyleClass().remove("top-action-btn-active");
            return;
        }
        notificationDropdownShowingOnPress = false;

        if (notificationDropdown == null) {
            loadNotificationDropdown();
        }
        if (notificationDropdown == null || notificationDropdownContent == null || notificationDropdownController == null) {
            return;
        }

        notificationDropdownController.refreshNotifications();
        showDropdown(notificationDropdown, notificationDropdownContent, btnNotification);
    }

    private void toggleUserDropdown() {
        if (userDropdownShowingOnPress || (userDropdown != null && userDropdown.isShowing())) {
            userDropdownShowingOnPress = false;
            hideUserDropdown();
            btnUser.getStyleClass().remove("top-action-btn-active");
            return;
        }
        userDropdownShowingOnPress = false;

        if (userDropdown == null) {
            loadUserDropdown();
        }
        if (userDropdown == null || userDropdownContent == null || userDropdownController == null) {
            return;
        }

        updateUserSummary();
        showDropdown(userDropdown, userDropdownContent, btnUser);
    }

    private void showDropdown(Popup dropdown, Parent content, Button anchor) {
        Bounds buttonBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        double popupWidth = content.prefWidth(-1);
        double x = buttonBounds.getMaxX() - popupWidth;
        double y = buttonBounds.getMaxY() + 8;
        dropdown.show(anchor, x, y);
    }

    private void hideNotificationDropdown() {
        if (notificationDropdown != null && notificationDropdown.isShowing()) {
            notificationDropdown.hide();
        }
    }

    private void hideUserDropdown() {
        if (userDropdown != null && userDropdown.isShowing()) {
            userDropdown.hide();
        }
    }

    private void loadNotificationDropdown() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/notification-dropdown.fxml"));
            notificationDropdownContent = loader.load();
            notificationDropdownController = loader.getController();

            notificationDropdown = new Popup();
            notificationDropdown.setAutoHide(true);
            notificationDropdown.setConsumeAutoHidingEvents(false);
            notificationDropdown.setHideOnEscape(true);
            notificationDropdown.setOnHidden(event -> btnNotification.getStyleClass().remove("top-action-btn-active"));
            notificationDropdown.getContent().add(notificationDropdownContent);
        } catch (Exception e) {
            System.out.println("Could not load notification dropdown!");
            e.printStackTrace();
        }
    }

    private void loadUserDropdown() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/user-dropdown.fxml"));
            userDropdownContent = loader.load();
            userDropdownController = loader.getController();

            userDropdown = new Popup();
            userDropdown.setAutoHide(true);
            userDropdown.setConsumeAutoHidingEvents(false);
            userDropdown.setHideOnEscape(true);
            userDropdown.setOnHidden(event -> btnUser.getStyleClass().remove("top-action-btn-active"));
            userDropdown.getContent().add(userDropdownContent);
        } catch (Exception e) {
            System.out.println("Could not load user dropdown!");
            e.printStackTrace();
        }
    }

    private void updateUserSummary() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "username";
        String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "email";

        userDropdownController.setUserSummary(username, email);
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
