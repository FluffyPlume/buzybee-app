package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import com.buzybee.app.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class DashboardController {
    @FXML private Label greetingLabel;
    @FXML private Label dashboardSubtitleLabel;
    @FXML private Label completedPercentLabel;
    @FXML private Label completedCountLabel;
    @FXML private Label hoursTrackedLabel;
    @FXML private Label pendingActionsLabel;
    @FXML private Label dueTodayLabel;
    @FXML private Label monthLabel;
    @FXML private DatePicker dashboardDatePicker;
    @FXML private VBox taskOverviewList;

    private AppShellController appShellController;

    @FXML
    public void initialize() {
        dashboardDatePicker.setValue(LocalDate.now());
        monthLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        loadDashboardData();
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

    private void loadDashboardData() {
        SessionManager session = SessionManager.getInstance();
        User currentUser = session.getCurrentUser();
        TaskDAO taskDAO = session.getTaskDao();

        String username = currentUser != null ? currentUser.getUsername() : "there";
        greetingLabel.setText("Hello, " + username + "!");

        if (taskDAO == null) {
            dashboardSubtitleLabel.setText("Sign in to see your workspace overview.");
            return;
        }

        List<Task> allTasks = taskDAO.getAllTasks();
        List<Task> pendingTasks = taskDAO.getPendingTasks();
        List<Task> scheduledTasks = taskDAO.getScheduledTasks();
        List<Task> backlogTasks = taskDAO.getBacklogTasks();
        List<Task> completedTasks = taskDAO.getCompletedTasks();

        int activeCount = pendingTasks.size() + scheduledTasks.size() + backlogTasks.size();
        int totalCount = allTasks.size();
        int completedPercent = totalCount == 0 ? 0 : (int) Math.round((completedTasks.size() * 100.0) / totalCount);
        long dueToday = allTasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .filter(task -> LocalDate.now().equals(task.getDeadline()))
                .count();
        double activeHours = allTasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .mapToDouble(Task::getEstimatedTime)
                .sum();

        completedPercentLabel.setText(completedPercent + "%");
        completedCountLabel.setText(completedTasks.size() + " completed of " + totalCount);
        hoursTrackedLabel.setText(String.format("%.1f hrs", activeHours));
        pendingActionsLabel.setText(activeCount + (activeCount == 1 ? " Task" : " Tasks"));
        dueTodayLabel.setText(dueToday + " due today");

        dashboardSubtitleLabel.setText(activeCount == 0
                ? "No active tasks right now. Nice and calm."
                : "You have " + activeCount + " active tasks across your workspace.");

        showTaskOverview(allTasks);
    }

    private void showTaskOverview(List<Task> tasks) {
        taskOverviewList.getChildren().clear();

        List<Task> activeTasks = tasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .sorted(Comparator
                        .comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Comparator.comparingDouble(Task::getPriorityScore).reversed()))
                .limit(4)
                .toList();

        if (activeTasks.isEmpty()) {
            Label emptyLabel = new Label("No active tasks yet. Add one from the Tasks tab.");
            emptyLabel.getStyleClass().add("empty-dashboard-label");
            taskOverviewList.getChildren().add(emptyLabel);
            return;
        }

        int index = 0;
        for (Task task : activeTasks) {
            taskOverviewList.getChildren().add(createTaskRow(task, index));
            index++;
        }
    }

    private HBox createTaskRow(Task task, int index) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("timeline-item", timelineStyle(index));

        Label status = new Label(task.getStatus());
        status.setPrefWidth(75);
        status.getStyleClass().add("timeline-time");

        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        VBox details = new VBox(2);
        Label title = new Label(task.getName());
        title.getStyleClass().add("timeline-title");
        Label subtitle = new Label(taskSubtitle(task));
        subtitle.getStyleClass().add("timeline-subtitle");
        details.getChildren().addAll(title, subtitle);

        row.getChildren().addAll(status, separator, details);
        return row;
    }

    private String taskSubtitle(Task task) {
        String deadline = task.getDeadline() == null ? "No deadline" : "Due " + task.getDeadline();
        return deadline + " | " + String.format("%.1f hrs", task.getEstimatedTime())
                + " | Priority " + String.format("%.2f", task.getPriorityScore());
    }

    private String timelineStyle(int index) {
        return switch (index % 3) {
            case 0 -> "timeline-gold";
            case 1 -> "timeline-brown";
            default -> "timeline-cream";
        };
    }
}
