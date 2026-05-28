package com.buzybee.app.controller;

import com.buzybee.app.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddEditTaskController {

    @FXML private Label lblModalTitle;
    @FXML private TextField txtName;
    @FXML private DatePicker dpDeadline;
    @FXML private TextField txtTime;
    @FXML private TextField txtDifficulty;
    @FXML private Label lblError;

    private Task taskToEdit = null;
    private Task createdTask = null;

    @FXML
    private void handleSave() {
        lblError.setText(""); // Clear old errors

        try {
            String name = txtName.getText();
            LocalDate deadline = dpDeadline.getValue();

            if (name == null || name.trim().isEmpty() || deadline == null) {
                lblError.setText("Please enter a name and select a deadline.");
                return;
            }

            double time = Double.parseDouble(txtTime.getText());
            double difficulty = Double.parseDouble(txtDifficulty.getText());

            if (taskToEdit == null) {
                createdTask = new Task(name, difficulty, time, deadline);
            } else {
                taskToEdit.setName(name);
                taskToEdit.setDeadline(deadline);
                taskToEdit.setEstimatedTime(time);
                taskToEdit.setDifficulty(difficulty);

                createdTask = taskToEdit;
            }

            closeWindow();

        } catch (NumberFormatException e) {
            lblError.setText("Time and Difficulty must be numbers (e.g. 2.5 or 8).");
        }
    }

    public void setTaskToEdit(Task task) {
        this.taskToEdit = task;

        if (lblModalTitle != null) {
            lblModalTitle.setText("Edit Task");
        }

        txtName.setText(task.getName());
        dpDeadline.setValue(task.getDeadline());
        txtTime.setText(String.valueOf(task.getEstimatedTime()));
        txtDifficulty.setText(String.valueOf(task.getDifficulty()));
    }

    @FXML
    private void handleCancel() {
        createdTask = null;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    public Task getCreatedTask() {
        return createdTask;
    }
}