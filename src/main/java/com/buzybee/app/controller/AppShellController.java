package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppShellController {
    private static final String DARK_MODE_STYLESHEET = "/com/buzybee/app/css/dark-mode.css";

    @FXML private BorderPane appShellRoot;
    @FXML private StackPane contentArea;
    @FXML private TopBarController topBarController;
    @FXML private SideMenuController sideMenuController;
    private boolean darkMode;

    @FXML
    private void initialize() {
        topBarController.setAppShellController(this);
        sideMenuController.setAppShellController(this);

        appShellRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node target = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;
            if (!topBarController.isTopActionTarget(target)) {
                topBarController.clearTopSelection();
            }
        });

        showDashboardHome();
    }

    void showDashboardHome() {
        topBarController.clearTopSelection();
        sideMenuController.selectDashboard();
        loadScreen("dashboard.fxml");
    }

    void showCalendar() {
        topBarController.clearTopSelection();
        sideMenuController.selectCalendar();
        loadScreen("calendar.fxml");
    }

    void showTasks() {
        topBarController.clearTopSelection();
        sideMenuController.selectTasks();
        loadScreen("tasks.fxml");
    }

    void showAnalytics() {
        topBarController.clearTopSelection();
        sideMenuController.selectAnalytics();
        loadScreen("analytics.fxml");
    }

    void showSettings() {
        topBarController.clearTopSelection();
        sideMenuController.selectSettings();
        clearContent();
    }

    void showLogout() {
        topBarController.clearTopSelection();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/log-out.fxml"));
            Parent dialogRoot = loader.load();
            if (darkMode) {
                dialogRoot.getStyleClass().add("dark-mode");
            }
            LogoutController logoutController = loader.getController();

            Stage dialog = new Stage();
            dialog.initOwner(appShellRoot.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setTitle("Log Out");

            Scene scene = new Scene(dialogRoot);
            scene.setFill(Color.TRANSPARENT);
            if (darkMode) {
                scene.getStylesheets().add(getClass().getResource(DARK_MODE_STYLESHEET).toExternalForm());
            }
            dialog.setScene(scene);

            logoutController.setAppShellController(this);
            logoutController.setDialogStage(dialog);

            // Center the dialog over the application window once its size is known.
            javafx.stage.Window owner = appShellRoot.getScene().getWindow();
            dialog.setOnShown(event -> {
                dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            });

            dialog.showAndWait();
        } catch (Exception e) {
            System.out.println("Could not load logout dialog!");
            e.printStackTrace();
        }
    }

    void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        appShellRoot.getStyleClass().remove("dark-mode");
        String darkModeStylesheet = getClass().getResource(DARK_MODE_STYLESHEET).toExternalForm();
        Scene scene = appShellRoot.getScene();

        if (darkMode) {
            appShellRoot.getStyleClass().add("dark-mode");
            if (scene != null && !scene.getStylesheets().contains(darkModeStylesheet)) {
                scene.getStylesheets().add(darkModeStylesheet);
            }
            addStylesheet(appShellRoot, darkModeStylesheet);
            contentArea.getChildren().forEach(node -> addStylesheet(node, darkModeStylesheet));
        } else {
            if (scene != null) {
                scene.getStylesheets().remove(darkModeStylesheet);
            }
            appShellRoot.getStylesheets().remove(darkModeStylesheet);
            contentArea.getChildren().forEach(node -> removeStylesheet(node, darkModeStylesheet));
        }
    }

    void logout() {
        topBarController.clearTopSelection();
        SessionManager.getInstance().endSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/auth.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) appShellRoot.getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root, 682, 383));
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.out.println("Could not load the login screen!");
            e.printStackTrace();
        }
    }

    private void loadScreen(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/" + fxmlFileName));
            Node newScreen = loader.load();
            Object controller = loader.getController();
            if (controller instanceof DashboardController dashboardController) {
                dashboardController.setAppShellController(this);
            } else if (controller instanceof AnalyticsController analyticsController) {
                analyticsController.setAppShellController(this);
            }
            if (darkMode) {
                addStylesheet(newScreen, getClass().getResource(DARK_MODE_STYLESHEET).toExternalForm());
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newScreen);
        } catch (Exception e) {
            System.out.println("Could not load the screen: " + fxmlFileName);
            e.printStackTrace();
        }
    }

    private void clearContent() {
        contentArea.getChildren().clear();
    }

    private void addStylesheet(Node node, String stylesheet) {
        if (node instanceof Parent parent && !parent.getStylesheets().contains(stylesheet)) {
            parent.getStylesheets().add(stylesheet);
        }
    }

    private void removeStylesheet(Node node, String stylesheet) {
        if (node instanceof Parent parent) {
            parent.getStylesheets().remove(stylesheet);
        }
    }
}
