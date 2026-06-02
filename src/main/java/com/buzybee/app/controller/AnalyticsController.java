package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

public class AnalyticsController {
    @FXML private Label analyticsSubtitleLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label completionCountLabel;
    @FXML private Label activeHoursLabel;
    @FXML private Label averageDifficultyLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label scheduledCountLabel;
    @FXML private Label backlogCountLabel;
    @FXML private Label completedCountLabel;
    @FXML private VBox priorityWatchList;

    private AppShellController appShellController;

    @FXML
    public void initialize() {
        loadAnalyticsData();
    }

    void setAppShellController(AppShellController appShellController) {
        this.appShellController = appShellController;
    }

    @FXML
    private void openTasks() {
        if (appShellController != null) {
            appShellController.showTasks();
        }
    }

    private void loadAnalyticsData() {
        TaskDAO taskDAO = SessionManager.getInstance().getTaskDao();

        if (taskDAO == null) {
            analyticsSubtitleLabel.setText("Sign in to see your analytics.");
            return;
        }

        List<Task> allTasks = taskDAO.getAllTasks();
        List<Task> pendingTasks = taskDAO.getPendingTasks();
        List<Task> scheduledTasks = taskDAO.getScheduledTasks();
        List<Task> backlogTasks = taskDAO.getBacklogTasks();
        List<Task> completedTasks = taskDAO.getCompletedTasks();

        int totalCount = allTasks.size();
        int activeCount = pendingTasks.size() + scheduledTasks.size() + backlogTasks.size();
        int completionRate = totalCount == 0 ? 0 : (int) Math.round((completedTasks.size() * 100.0) / totalCount);
        double activeHours = allTasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .mapToDouble(Task::getEstimatedTime)
                .sum();
        double averageDifficulty = allTasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .mapToDouble(Task::getDifficulty)
                .average()
                .orElse(0);

        completionRateLabel.setText(completionRate + "%");
        completionCountLabel.setText(completedTasks.size() + " completed of " + totalCount);
        activeHoursLabel.setText(String.format("%.1f hrs", activeHours));
        averageDifficultyLabel.setText(String.format("%.1f", averageDifficulty));

        pendingCountLabel.setText(String.valueOf(pendingTasks.size()));
        scheduledCountLabel.setText(String.valueOf(scheduledTasks.size()));
        backlogCountLabel.setText(String.valueOf(backlogTasks.size()));
        completedCountLabel.setText(String.valueOf(completedTasks.size()));

        analyticsSubtitleLabel.setText(activeCount == 0
                ? "No active tasks to analyze yet."
                : "Tracking " + activeCount + " active tasks across your workspace.");

        showPriorityWatch(allTasks);
    }

    private void showPriorityWatch(List<Task> tasks) {
        priorityWatchList.getChildren().clear();

        List<Task> topTasks = tasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .sorted(Comparator.comparingDouble(Task::getPriorityScore).reversed())
                .limit(5)
                .toList();

        if (topTasks.isEmpty()) {
            Label emptyLabel = new Label("No active tasks to prioritize.");
            emptyLabel.getStyleClass().add("analytics-empty");
            priorityWatchList.getChildren().add(emptyLabel);
            return;
        }

        for (Task task : topTasks) {
            priorityWatchList.getChildren().add(createPriorityRow(task));
        }
    }

    private HBox createPriorityRow(Task task) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("analytics-row");

        VBox details = new VBox(2);
        Label name = new Label(task.getName());
        name.getStyleClass().add("analytics-row-value");
        String deadline = task.getDeadline() == null ? "No deadline" : "Due " + task.getDeadline();
        Label meta = new Label(task.getStatus() + " | " + deadline);
        meta.getStyleClass().add("analytics-card-note");
        details.getChildren().addAll(name, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label score = new Label(String.format("%.2f", task.getPriorityScore()));
        score.getStyleClass().add("analytics-row-value");

        row.getChildren().addAll(details, spacer, score);
        return row;
    }
}
