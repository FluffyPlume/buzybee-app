package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.util.SystemClock;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationDropdownController {
    private static final int MAX_NOTIFICATIONS = 6;

    @FXML private VBox notificationList;

    void refreshNotifications() {
        notificationList.getChildren().clear();

        TaskDAO taskDAO = SessionManager.getInstance().getTaskDao();
        if (taskDAO == null) {
            showEmptyState();
            return;
        }

        LocalDate today = SystemClock.today();
        List<Task> allTasks = taskDAO.getAllTasks() == null ? List.of() : taskDAO.getAllTasks();
        List<TimeBlock> schedule = taskDAO.getAllTimeBlocks() == null ? List.of() : taskDAO.getAllTimeBlocks();
        List<NotificationItem> items = new ArrayList<>();

        addAvailabilityNotifications(items, schedule, allTasks, today);
        addScheduledTaskNotifications(items, schedule, today);
        addDeadlineNotifications(items, allTasks, today);

        List<NotificationItem> visibleItems = items.stream()
                .sorted(Comparator
                        .comparingInt(NotificationItem::rank)
                        .thenComparing(NotificationItem::date, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(NotificationItem::title, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_NOTIFICATIONS)
                .toList();

        if (visibleItems.isEmpty()) {
            showEmptyState();
            return;
        }

        visibleItems.forEach(item -> notificationList.getChildren().add(createNotificationRow(item)));
    }

    private void addAvailabilityNotifications(List<NotificationItem> items, List<TimeBlock> schedule, List<Task> allTasks, LocalDate today) {
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        boolean hasCurrentWeekAvailability = schedule.stream()
                .anyMatch(block -> weekStart.equals(block.getDate()));
        boolean hasActiveTasks = allTasks.stream().anyMatch(task -> !"COMPLETED".equals(task.getStatus()));

        if (!hasCurrentWeekAvailability && today.getDayOfWeek() == DayOfWeek.MONDAY && !SystemClock.now().isBefore(java.time.LocalTime.of(8, 0))) {
            items.add(new NotificationItem(
                    "Set weekly availability",
                    "Add your free hours so BuzyBee can schedule this week.",
                    "notification-dot-warning",
                    0,
                    today));
        }

        boolean hasAssignmentsThisWeek = schedule.stream()
                .filter(block -> block.getDate() != null)
                .filter(block -> !block.getDate().isBefore(weekStart) && !block.getDate().isAfter(weekStart.plusDays(6)))
                .anyMatch(block -> !block.getAssignedTasks().isEmpty());

        if (hasCurrentWeekAvailability && hasActiveTasks && !hasAssignmentsThisWeek) {
            items.add(new NotificationItem(
                    "Run task schedule",
                    "Use Frontload or Backload to assign tasks into your available hours.",
                    "notification-dot-calm",
                    2,
                    today));
        }
    }

    private void addScheduledTaskNotifications(List<NotificationItem> items, List<TimeBlock> schedule, LocalDate today) {
        Set<String> seenTaskDates = new HashSet<>();
        for (TimeBlock block : schedule) {
            if (block.getDate() == null || block.getAssignedTasks().isEmpty()) {
                continue;
            }

            long daysAway = ChronoUnit.DAYS.between(today, block.getDate());
            if (daysAway < 0 || daysAway > 7) {
                continue;
            }

            for (Task task : block.getAssignedTasks()) {
                if ("COMPLETED".equals(task.getStatus())) {
                    continue;
                }
                String key = task.getId() + "@" + block.getDate();
                if (!seenTaskDates.add(key)) {
                    continue;
                }

                items.add(new NotificationItem(
                        task.getName(),
                        scheduledDetail(block.getDate(), task),
                        daysAway == 0 ? "notification-dot-warning" : "notification-dot-calm",
                        daysAway == 0 ? 1 : 4,
                        block.getDate()));
            }
        }
    }

    private void addDeadlineNotifications(List<NotificationItem> items, List<Task> allTasks, LocalDate today) {
        for (Task task : allTasks) {
            if (task.getDeadline() == null || "COMPLETED".equals(task.getStatus())) {
                continue;
            }

            long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
            if (!"BACKLOG".equals(task.getStatus()) && daysLeft > 3) {
                continue;
            }

            items.add(new NotificationItem(
                    task.getName(),
                    deadlineDetail(task, today),
                    deadlineDotStyle(task, today),
                    deadlineRank(task, today),
                    task.getDeadline()));
        }
    }

    private HBox createNotificationRow(NotificationItem item) {
        HBox row = new HBox(9);
        row.getStyleClass().add("notification-row");

        Region dot = new Region();
        dot.getStyleClass().add("notification-dot");
        dot.getStyleClass().add(item.dotStyleClass());

        VBox textBox = new VBox(1);
        textBox.getStyleClass().add("notification-text");

        Label title = new Label(item.title());
        title.getStyleClass().add("notification-title");
        title.setWrapText(true);

        Label detail = new Label(item.detail());
        detail.getStyleClass().add("notification-detail");
        detail.setWrapText(true);

        textBox.getChildren().addAll(title, detail);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        row.getChildren().addAll(dot, textBox);
        return row;
    }

    private String scheduledDetail(LocalDate scheduledDate, Task task) {
        LocalDate today = SystemClock.today();
        long daysAway = ChronoUnit.DAYS.between(today, scheduledDate);
        String dayText;
        if (daysAway == 0) {
            dayText = "Scheduled today";
        } else if (daysAway == 1) {
            dayText = "Scheduled tomorrow";
        } else {
            dayText = "Scheduled " + scheduledDate.format(DateTimeFormatter.ofPattern("MMM d"));
        }
        return dayText + " | " + String.format("%.1f hrs", task.getEstimatedTime());
    }

    private String deadlineDetail(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        if ("BACKLOG".equals(task.getStatus()) || daysLeft < 0) {
            long overdueDays = Math.abs(daysLeft);
            return overdueDays == 1 ? "Deadline passed by 1 day" : "Deadline passed by " + overdueDays + " days";
        }
        if (daysLeft == 0) {
            return "Deadline is today.";
        }
        if (daysLeft == 1) {
            return "Deadline is tomorrow.";
        }
        return "Deadline in " + daysLeft + " days.";
    }

    private String deadlineDotStyle(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        if ("BACKLOG".equals(task.getStatus()) || daysLeft < 0) {
            return "notification-dot-danger";
        }
        if (daysLeft <= 1) {
            return "notification-dot-warning";
        }
        return "notification-dot-calm";
    }

    private int deadlineRank(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
        if ("BACKLOG".equals(task.getStatus()) || daysLeft < 0) {
            return 0;
        }
        if (daysLeft == 0) {
            return 1;
        }
        return 3;
    }

    private void showEmptyState() {
        Label empty = new Label("No task notifications right now.");
        empty.getStyleClass().add("notification-empty");
        empty.setWrapText(true);
        notificationList.getChildren().add(empty);
    }

    private record NotificationItem(String title, String detail, String dotStyleClass, int rank, LocalDate date) { }
}
