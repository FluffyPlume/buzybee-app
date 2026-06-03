package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

public class DashboardController {
    private enum TaskFilter {
        ALL, PENDING, DONE
    }

    @FXML private StackPane dashboardRoot;

    private AppShellController appShellController;
    private TaskDAO taskDAO;
    private TaskFilter currentFilter = TaskFilter.ALL;
    private YearMonth displayedMonth = YearMonth.now();
    private Timeline timerTimeline;
    private int timerSeconds;
    private boolean timerRunning;

    private Label greetingLabel;
    private Label pendingSubtitleLabel;
    private Label completedValueLabel;
    private Label completedSubtextLabel;
    private ProgressBar completedProgress;
    private Label pendingValueLabel;
    private Label pendingSubtextLabel;
    private VBox taskList;
    private Label activeTaskLabel;
    private TextField timerField;
    private Button startPauseButton;
    private Label calendarMonthLabel;
    private GridPane calendarGrid;
    private Button allFilterButton;
    private Button pendingFilterButton;
    private Button doneFilterButton;

    @FXML
    public void initialize() {
        taskDAO = SessionManager.getInstance().getTaskDao();
        dashboardRoot.getChildren().setAll(createDashboard());
        setupTimer();
        refreshDashboard();
        refreshCalendar();
    }

    void setAppShellController(AppShellController appShellController) {
        this.appShellController = appShellController;
    }

    private Node createDashboard() {
        HBox columns = new HBox(18);
        columns.setAlignment(Pos.TOP_LEFT);
        columns.getStyleClass().add("dashboard-page");
        StackPane.setAlignment(columns, Pos.TOP_LEFT);

        VBox leftColumn = new VBox(16);
        leftColumn.setMinWidth(620);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox rightColumn = new VBox(14);
        rightColumn.setPrefWidth(280);
        rightColumn.setMinWidth(260);

        leftColumn.getChildren().addAll(createGreetingRow(), createStatsRow(), createTaskOverviewCard());
        rightColumn.getChildren().addAll(createFocusTimer(), createDailyStreak(), createMiniCalendar());
        columns.getChildren().addAll(leftColumn, rightColumn);
        return columns;
    }

    private Node createGreetingRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox copy = new VBox(3);
        greetingLabel = new Label();
        greetingLabel.getStyleClass().add("dashboard-greeting");
        pendingSubtitleLabel = new Label();
        pendingSubtitleLabel.getStyleClass().add("dashboard-subtitle");
        copy.getChildren().addAll(greetingLabel, pendingSubtitleLabel);

        Button addButton = new Button("\u002b  Add task");
        addButton.getStyleClass().add("add-task-button");
        addButton.setOnAction(event -> showAddTaskModal());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(copy, spacer, addButton);
        return row;
    }

    private Node createStatsRow() {
        HBox row = new HBox(12);
        row.getChildren().addAll(
                createCompletedCard(),
                createStatCard("\u25f7", "Hours tracked", "2.5", "Active workload", null),
                createPendingCard()
        );
        return row;
    }

    private Node createCompletedCard() {
        VBox card = createCard("stat-card");
        Label label = createStatLabel("\u2713", "Completed");
        completedValueLabel = createStatValue();
        completedSubtextLabel = new Label();
        completedSubtextLabel.getStyleClass().add("stat-subtext");
        completedProgress = new ProgressBar(0);
        completedProgress.setMaxWidth(Double.MAX_VALUE);
        completedProgress.getStyleClass().add("completed-progress");
        card.getChildren().addAll(label, completedValueLabel, completedSubtextLabel, completedProgress);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Node createPendingCard() {
        VBox card = createCard("stat-card");
        pendingValueLabel = createStatValue();
        pendingSubtextLabel = new Label();
        pendingSubtextLabel.getStyleClass().add("stat-subtext");
        card.getChildren().addAll(createStatLabel("\u26a0", "Pending"), pendingValueLabel, pendingSubtextLabel);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Node createStatCard(String icon, String labelText, String valueText, String subtext, Node extra) {
        VBox card = createCard("stat-card");
        Label value = createStatValue();
        value.setText(valueText);
        Label footer = new Label(subtext);
        footer.getStyleClass().add("stat-subtext");
        card.getChildren().addAll(createStatLabel(icon, labelText), value, footer);
        if (extra != null) {
            card.getChildren().add(extra);
        }
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Label createStatLabel(String icon, String text) {
        Label label = new Label(icon + "  " + text.toUpperCase(Locale.ROOT));
        label.getStyleClass().add("stat-label");
        return label;
    }

    private Label createStatValue() {
        Label label = new Label("0");
        label.getStyleClass().add("stat-value");
        return label;
    }

    private VBox createTaskOverviewCard() {
        VBox card = createCard("task-overview-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Today's task overview");
        title.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox filters = new HBox(6);
        allFilterButton = createFilterButton("All", TaskFilter.ALL);
        pendingFilterButton = createFilterButton("Pending", TaskFilter.PENDING);
        doneFilterButton = createFilterButton("Done", TaskFilter.DONE);
        filters.getChildren().addAll(allFilterButton, pendingFilterButton, doneFilterButton);
        header.getChildren().addAll(title, spacer, filters);

        taskList = new VBox();
        VBox.setVgrow(taskList, Priority.ALWAYS);
        card.getChildren().addAll(header, taskList);
        return card;
    }

    private Button createFilterButton(String text, TaskFilter filter) {
        Button button = new Button(text);
        button.getStyleClass().add("filter-tab");
        button.setOnAction(event -> {
            currentFilter = filter;
            refreshDashboard();
        });
        return button;
    }

    private Node createFocusTimer() {
        VBox card = new VBox(10);
        card.getStyleClass().add("focus-card");
        Label title = new Label("\u25b6  Focus timer");
        title.getStyleClass().add("focus-title");
        activeTaskLabel = new Label("No task selected");
        activeTaskLabel.getStyleClass().add("focus-task");
        activeTaskLabel.setOnMouseClicked(event -> showTaskPickerModal());
        timerField = new TextField();
        timerField.getStyleClass().add("timer-display");
        setupTimerFieldFormatter();
        timerField.setOnAction(event -> applyTimerInput());

        HBox buttons = new HBox(8);
        startPauseButton = new Button("\u25b6  Start");
        startPauseButton.getStyleClass().add("timer-button");
        startPauseButton.setOnAction(event -> toggleTimer());
        Button resetButton = new Button("\u21bb  Reset");
        resetButton.getStyleClass().addAll("timer-button", "timer-reset-button");
        resetButton.setOnAction(event -> resetTimer());
        buttons.getChildren().addAll(startPauseButton, resetButton);

        card.getChildren().addAll(title, activeTaskLabel, timerField, buttons);
        return card;
    }

    private Node createDailyStreak() {
        VBox card = createCard("side-card");
        Label title = new Label("Daily streak");
        title.getStyleClass().add("side-title");
        HBox streakCopy = new HBox(10);
        streakCopy.setAlignment(Pos.CENTER_LEFT);
        Label count = new Label("7");
        count.getStyleClass().add("streak-count");
        Label text = new Label("days in a row!\nKeep it up, bee!");
        text.getStyleClass().add("streak-copy");
        streakCopy.getChildren().addAll(count, text);

        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER_LEFT);
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < days.length; i++) {
            Label dot = new Label(days[i]);
            dot.getStyleClass().add(i < 6 ? "streak-dot-done" : "streak-dot-today");
            dots.getChildren().add(dot);
        }

        card.getChildren().addAll(title, streakCopy, dots);
        return card;
    }

    private Node createMiniCalendar() {
        VBox card = createCard("side-card");
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER);
        Button prev = new Button("\u2039");
        Button next = new Button("\u203a");
        prev.getStyleClass().add("calendar-nav");
        next.getStyleClass().add("calendar-nav");
        calendarMonthLabel = new Label();
        calendarMonthLabel.getStyleClass().add("calendar-title");
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        prev.setOnAction(event -> {
            displayedMonth = displayedMonth.minusMonths(1);
            refreshCalendar();
        });
        next.setOnAction(event -> {
            displayedMonth = displayedMonth.plusMonths(1);
            refreshCalendar();
        });
        header.getChildren().addAll(prev, leftSpacer, calendarMonthLabel, rightSpacer, next);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(3);
        calendarGrid.setVgap(4);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7.0);
            column.setHalignment(HPos.CENTER);
            calendarGrid.getColumnConstraints().add(column);
        }

        card.getChildren().addAll(header, calendarGrid);
        return card;
    }

    private VBox createCard(String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add(styleClass);
        return card;
    }

    private void refreshDashboard() {
        List<Task> tasks = tasks();
        List<Task> todayTasks = todayTasks(tasks);
        long done = todayTasks.stream().filter(this::isDone).count();
        long pending = todayTasks.size() - done;

        greetingLabel.setText(timeGreeting() + ", bee!");
        pendingSubtitleLabel.setText("You have " + pending + " tasks pending today.");
        completedValueLabel.setText(String.valueOf(done));
        completedSubtextLabel.setText("of " + todayTasks.size() + " tasks");
        completedProgress.setProgress(todayTasks.isEmpty() ? 0 : (double) done / todayTasks.size());
        pendingValueLabel.setText(String.valueOf(pending));
        pendingSubtextLabel.setText(pending + " due today");

        updateFilterStyles();
        refreshTaskRows(todayTasks);
        refreshCalendar();
    }

    private void refreshTaskRows(List<Task> tasks) {
        taskList.getChildren().clear();
        List<Task> visibleTasks = tasks.stream()
                .filter(task -> switch (currentFilter) {
                    case ALL -> true;
                    case PENDING -> !isDone(task);
                    case DONE -> isDone(task);
                })
                .sorted(Comparator
                        .comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (visibleTasks.isEmpty()) {
            Label empty = new Label("No tasks here yet!");
            empty.getStyleClass().add("empty-dashboard-label");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            taskList.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < visibleTasks.size(); i++) {
            taskList.getChildren().add(createTaskRow(visibleTasks.get(i), i == visibleTasks.size() - 1));
        }
    }

    private Node createTaskRow(Task task, boolean lastRow) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("task-row");
        if (lastRow) {
            row.getStyleClass().add("task-row-last");
        }

        Button check = new Button(isDone(task) ? "\u2713" : "");
        check.getStyleClass().add(isDone(task) ? "task-check-done" : "task-check");
        check.setOnAction(event -> {
            if (isDone(task)) {
                task.setStatus("PENDING");
                task.setDateCompleted(null);
            } else {
                task.setStatus("COMPLETED");
                task.setDateCompleted(LocalDate.now());
            }
            saveTasks();
            refreshDashboard();
        });

        Label name = new Label(task.getName());
        name.getStyleClass().add(isDone(task) ? "task-name-done" : "task-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label tag = new Label(displayTag(task));
        tag.getStyleClass().addAll("tag-pill", "tag-" + tagStyle(task));

        Label due = new Label(task.getDeadline() == null ? "No due date" : task.getDeadline().format(DateTimeFormatter.ofPattern("MMM d")));
        due.getStyleClass().add("task-due");

        Button focus = new Button("\u25b6");
        focus.getStyleClass().add("focus-task-button");
        focus.setOnAction(event -> activeTaskLabel.setText(task.getName()));

        row.getChildren().addAll(check, name, tag, due, focus);
        return row;
    }

    private void updateFilterStyles() {
        styleFilterButton(allFilterButton, TaskFilter.ALL);
        styleFilterButton(pendingFilterButton, TaskFilter.PENDING);
        styleFilterButton(doneFilterButton, TaskFilter.DONE);
    }

    private void styleFilterButton(Button button, TaskFilter filter) {
        button.getStyleClass().remove("filter-tab-active");
        if (currentFilter == filter) {
            button.getStyleClass().add("filter-tab-active");
        }
    }

    private void refreshCalendar() {
        if (calendarGrid == null) {
            return;
        }

        calendarMonthLabel.setText(displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarGrid.getChildren().clear();
        String[] weekDays = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < weekDays.length; i++) {
            Label label = new Label(weekDays[i]);
            label.getStyleClass().add("calendar-weekday");
            calendarGrid.add(label, i, 0);
        }

        LocalDate firstVisible = displayedMonth.atDay(1)
                .minusDays(displayedMonth.atDay(1).getDayOfWeek().getValue() - 1L);
        for (int i = 0; i < 42; i++) {
            LocalDate date = firstVisible.plusDays(i);
            VBox cell = new VBox(1);
            cell.setAlignment(Pos.CENTER);
            cell.getStyleClass().add("calendar-day");
            if (!YearMonth.from(date).equals(displayedMonth)) {
                cell.getStyleClass().add("calendar-overflow");
            }
            if (date.equals(LocalDate.now())) {
                cell.getStyleClass().add("calendar-today");
            }

            Label day = new Label(String.valueOf(date.getDayOfMonth()));
            day.getStyleClass().add("calendar-day-number");
            Region dot = new Region();
            dot.getStyleClass().add("calendar-task-dot");
            dot.setVisible(hasTaskOn(date));
            cell.getChildren().addAll(day, dot);
            calendarGrid.add(cell, i % 7, (i / 7) + 1);
        }
    }

    private void showAddTaskModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/add-task-modal.fxml"));
            StackPane overlay = loader.load();
            AddTaskModalController controller = loader.getController();
            controller.configure(taskDAO, this::refreshDashboard);
            dashboardRoot.getChildren().add(overlay);
            controller.requestInitialFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showTaskPickerModal() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("modal-overlay");
        overlay.getStylesheets().add(getClass().getResource("/com/buzybee/app/css/add-task-modal.css").toExternalForm());
        overlay.setPickOnBounds(true);

        VBox modal = new VBox(10);
        modal.getStyleClass().add("add-task-modal");
        modal.setMaxWidth(320);
        Label title = new Label("Choose a focus task");
        title.getStyleClass().add("modal-title");

        VBox rows = new VBox(4);
        List<Task> selectableTasks = tasks().stream()
                .filter(task -> !isDone(task))
                .sorted(Comparator
                        .comparing((Task task) -> !LocalDate.now().equals(task.getDeadline()))
                        .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (selectableTasks.isEmpty()) {
            Label empty = new Label("No pending tasks to focus on.");
            empty.getStyleClass().add("empty-dashboard-label");
            rows.getChildren().add(empty);
        } else {
            for (Task task : selectableTasks) {
                Button taskButton = new Button(task.getName());
                taskButton.setMaxWidth(Double.MAX_VALUE);
                taskButton.getStyleClass().add("task-picker-row");
                taskButton.setOnAction(event -> {
                    activeTaskLabel.setText(task.getName());
                    dashboardRoot.getChildren().remove(overlay);
                });
                rows.getChildren().add(taskButton);
            }
        }

        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("modal-cancel-button");
        cancel.setOnAction(event -> dashboardRoot.getChildren().remove(overlay));
        actions.getChildren().add(cancel);

        modal.getChildren().addAll(title, rows, actions);
        overlay.getChildren().add(modal);
        dashboardRoot.getChildren().add(overlay);
    }

    private void setupTimer() {
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (timerSeconds > 0) {
                timerSeconds--;
                updateTimerLabel();
            } else {
                timerTimeline.stop();
                timerRunning = false;
                updateTimerButton();
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void toggleTimer() {
        if (!timerRunning && timerSeconds <= 0 && !applyTimerInput()) {
            return;
        }
        timerRunning = !timerRunning;
        if (timerRunning) {
            timerField.setEditable(false);
            timerTimeline.play();
        } else {
            timerTimeline.pause();
            timerField.setEditable(true);
        }
        updateTimerButton();
    }

    private void resetTimer() {
        timerTimeline.stop();
        timerRunning = false;
        timerSeconds = 0;
        timerField.setText("00:00:00");
        timerField.setEditable(true);
        updateTimerButton();
    }

    private boolean applyTimerInput() {
        timerSeconds = parseTimerText(timerField.getText());
        if (timerSeconds <= 0) {
            timerField.setText("00:00:00");
            return false;
        }
        updateTimerLabel();
        return true;
    }

    private void updateTimerLabel() {
        timerField.setText(formatTimerText());
    }

    private void setupTimerFieldFormatter() {
        timerField.setText("00:00:00");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String digits = change.getControlNewText().replaceAll("[^0-9]", "");
            if (digits.length() > 6) {
                digits = digits.substring(digits.length() - 6);
            }

            String formatted = formatTimerDigits(digits);
            change.setRange(0, change.getControlText().length());
            change.setText(formatted);
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());
            return change;
        };
        timerField.setTextFormatter(new TextFormatter<>(filter));
    }

    private int parseTimerText(String text) {
        String[] parts = text == null ? new String[0] : text.split(":");
        if (parts.length != 3) {
            return 0;
        }
        int hours = Integer.parseInt(parts[0]);
        int minutes = Math.min(59, Integer.parseInt(parts[1]));
        int seconds = Math.min(59, Integer.parseInt(parts[2]));
        return (hours * 3600) + (minutes * 60) + seconds;
    }

    private String formatTimerText() {
        int hours = timerSeconds / 3600;
        int minutes = (timerSeconds % 3600) / 60;
        int seconds = timerSeconds % 60;
        return String.format("%02d:%02d:%02d", Math.min(99, hours), minutes, seconds);
    }

    private String formatTimerDigits(String digits) {
        String padded = ("000000" + digits).substring(("000000" + digits).length() - 6);
        int hours = Integer.parseInt(padded.substring(0, 2));
        int minutes = Math.min(59, Integer.parseInt(padded.substring(2, 4)));
        int seconds = Math.min(59, Integer.parseInt(padded.substring(4, 6)));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateTimerButton() {
        startPauseButton.setText(timerRunning ? "\u23f8  Pause" : "\u25b6  Start");
    }

    private List<Task> tasks() {
        return taskDAO == null || taskDAO.getAllTasks() == null ? List.of() : taskDAO.getAllTasks();
    }

    private List<Task> todayTasks(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(task -> today.equals(task.getDeadline()))
                .toList();
    }

    private void saveTasks() {
        if (taskDAO != null) {
            taskDAO.saveChanges();
        }
    }

    private boolean isDone(Task task) {
        return "COMPLETED".equals(task.getStatus());
    }

    private boolean hasTaskOn(LocalDate date) {
        return tasks().stream().anyMatch(task -> date.equals(task.getDeadline()));
    }

    private String displayTag(Task task) {
        String tag = task.getTag();
        if (tag == null || tag.isBlank()) {
            return "work";
        }
        return tag.toLowerCase(Locale.ROOT);
    }

    private String tagStyle(Task task) {
        return switch (displayTag(task)) {
            case "work", "personal", "urgent" -> displayTag(task);
            default -> "custom";
        };
    }

    private String timeGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 18) {
            return "Good afternoon";
        }
        return "Good evening";
    }
}
