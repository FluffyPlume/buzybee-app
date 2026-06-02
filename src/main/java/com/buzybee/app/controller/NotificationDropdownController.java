package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import com.buzybee.app.util.SystemClock;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public class NotificationDropdownController {
    private static final int MAX_NOTIFICATIONS = 5;

    @FXML private VBox notificationList;

    void refreshNotifications() {
        notificationList.getChildren().clear();

        TaskDAO taskDAO = SessionManager.getInstance().getTaskDao();
        if (taskDAO == null || taskDAO.getAllTasks() == null || taskDAO.getAllTasks().isEmpty()) {
            showEmptyState();
            return;
        }

        LocalDate today = SystemClock.today();
        List<Task> notificationTasks = taskDAO.getAllTasks().stream()
                .filter(task -> task.getDeadline() != null)
                .filter(task -> !"COMPLETED".equals(task.getStatus()))
                .filter(task -> shouldNotify(task, today))
                .sorted(Comparator
                        .comparingInt((Task task) -> urgencyRank(task, today))
                        .thenComparing(Task::getDeadline)
                        .thenComparing(Task::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_NOTIFICATIONS)
                .toList();

        if (notificationTasks.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Task task : notificationTasks) {
            notificationList.getChildren().add(createNotificationRow(task, today));
        }
    }

    private boolean shouldNotify(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        return "BACKLOG".equals(task.getStatus())
                || task.getDeadline().isBefore(today)
                || daysLeft <= 3
                || "SCHEDULED".equals(task.getStatus());
    }

    private int urgencyRank(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        if ("BACKLOG".equals(task.getStatus()) || daysLeft < 0) {
            return 0;
        }
        if (daysLeft == 0) {
            return 1;
        }
        if (daysLeft <= 3) {
            return 2;
        }
        if ("SCHEDULED".equals(task.getStatus())) {
            return 3;
        }
        return 4;
    }

    private HBox createNotificationRow(Task task, LocalDate today) {
        HBox row = new HBox(9);
        row.getStyleClass().add("notification-row");

        Region dot = new Region();
        dot.getStyleClass().add("notification-dot");
        dot.getStyleClass().add(dotStyleClass(task, today));

        VBox textBox = new VBox(1);
        textBox.getStyleClass().add("notification-text");

        Label title = new Label(task.getName());
        title.getStyleClass().add("notification-title");
        title.setWrapText(true);

        Label detail = new Label(buildDetailText(task, today));
        detail.getStyleClass().add("notification-detail");
        detail.setWrapText(true);

        textBox.getChildren().addAll(title, detail);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        row.getChildren().addAll(dot, textBox);
        return row;
    }

    private String dotStyleClass(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        if ("BACKLOG".equals(task.getStatus()) || daysLeft < 0) {
            return "notification-dot-danger";
        }
        if (daysLeft == 0) {
            return "notification-dot-warning";
        }
        return "notification-dot-calm";
    }

    private String buildDetailText(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        if ("BACKLOG".equals(task.getStatus()) || daysLeft < 0) {
            long overdueDays = Math.abs(daysLeft);
            return overdueDays == 1 ? "Late by 1 day" : "Late by " + overdueDays + " days";
        }
        if (daysLeft == 0) {
            return "Due today. Finish this when you can.";
        }
        if (daysLeft == 1) {
            return "Due tomorrow.";
        }
        if (daysLeft <= 3) {
            return "Due in " + daysLeft + " days.";
        }
        return "Scheduled task due " + task.getDeadline() + ".";
    }

    private void showEmptyState() {
        Label empty = new Label("No task notifications right now.");
        empty.getStyleClass().add("notification-empty");
        empty.setWrapText(true);
        notificationList.getChildren().add(empty);
    }
}
