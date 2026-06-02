package com.buzybee.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LogoutController {

    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private AppShellController appShellController;
    private Stage dialogStage;

    public void setAppShellController(AppShellController appShellController) {
        this.appShellController = appShellController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void onCancel() {
        // "Stay" — just close the dialog and leave the user where they were.
        closeDialog();
    }

    @FXML
    private void onConfirmLogout() {
        // Close the dialog first, then hand off to the shell to end the session
        // and return to the login screen.
        closeDialog();
        if (appShellController != null) {
            appShellController.logout();
        }
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
