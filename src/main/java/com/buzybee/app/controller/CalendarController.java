package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.exception.InvalidTaskException;
import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.service.TaskService;
import com.buzybee.app.util.SystemClock;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarController {

    private int dynamicStartHour = 8;
    private int dynamicEndHour = 22;

    @FXML private StackPane calendarRoot;
    @FXML private Button dailyViewButton;
    @FXML private Button weeklyViewButton;
    @FXML private Button monthlyViewButton;
    @FXML private Button frontloadButton;
    @FXML private Button backloadButton;
    @FXML private Label miniMonthLabel;
    @FXML private GridPane miniMonthGrid;
    @FXML private Label selectedDateLabel;
    @FXML private VBox selectedDayTaskList;
    @FXML private VBox filterList;
    @FXML private Label headerDateLabel;
    @FXML private GridPane calendarGrid;

    private final DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("MMM d");
    private final DateTimeFormatter miniMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter selectedDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private final Set<String> selectedTags = new HashSet<>();

    private TaskService taskService;
    private TaskDAO taskDAO;
    private LocalDate weekStart;
    private LocalDate selectedDate;
    private YearMonth displayedMonth;
    private CalendarView currentView = CalendarView.WEEKLY;
    private ScheduleMode scheduleMode = ScheduleMode.BACKLOAD;
    private LocalDate lastAvailabilityPromptWeekStart;
    private Timeline availabilityWatcher;

    private enum CalendarView {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    private enum ScheduleMode {
        FRONTLOAD,
        BACKLOAD
    }

    @FXML
    private void initialize() {
        taskService = new TaskService();
        taskDAO = SessionManager.getInstance().getTaskDao();
        selectedDate = LocalDate.now();
        weekStart = selectedDate;
        displayedMonth = YearMonth.from(selectedDate);

        selectedTags.addAll(getTaskTags());
        renderAll();
        Platform.runLater(this::promptForWeeklyAvailabilityIfDue);
        startWeeklyAvailabilityWatcher();
    }

    @FXML
    private void handlePrevMonth() {
        displayedMonth = displayedMonth.minusMonths(1);
        renderMiniCalendar();
    }

    @FXML
    private void handleNextMonth() {
        displayedMonth = displayedMonth.plusMonths(1);
        renderMiniCalendar();
    }

    @FXML
    private void handleSetAvailability() {
        showAvailabilityDialog(getSchedulingWeekStart(selectedDate));
    }

    @FXML
    private void handleFrontloadMode() {
        scheduleMode = ScheduleMode.FRONTLOAD;
        renderModeButtons();
    }

    @FXML
    private void handleBackloadMode() {
        scheduleMode = ScheduleMode.BACKLOAD;
        renderModeButtons();
    }

    @FXML
    private void handleRunSchedule() {
        LocalDate scheduleStart = getSchedulingWeekStart(selectedDate);
        if (!hasScheduleForWeek(scheduleStart)) {
            boolean saved = showAvailabilityDialog(scheduleStart);
            if (!saved) {
                return;
            }
        }

        try {
            taskService.generateWeeklySchedule(scheduleMode.name());
            weekStart = scheduleStart;
            selectedDate = scheduleStart;
            displayedMonth = YearMonth.from(selectedDate);
            currentView = CalendarView.WEEKLY;
            renderAll();
            showInfo("Schedule generated", "Tasks were prioritized and assigned with " + capitalize(scheduleMode.name()) + " mode.");
        } catch (Exception e) {
            showError("Could not generate schedule", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDailyView() {
        currentView = CalendarView.DAILY;
        renderViewButtons();
        renderCalendar();
    }

    @FXML
    private void handleWeeklyView() {
        currentView = CalendarView.WEEKLY;
        weekStart = selectedDate;
        renderViewButtons();
        renderCalendar();
    }

    @FXML
    private void handleMonthlyView() {
        currentView = CalendarView.MONTHLY;
        displayedMonth = YearMonth.from(selectedDate);
        renderViewButtons();
        renderCalendar();
    }

    @FXML
    private void handlePrevWeek() {
        if (currentView == CalendarView.DAILY) {
            selectedDate = selectedDate.minusDays(1);
            weekStart = selectedDate;
        } else if (currentView == CalendarView.MONTHLY) {
            selectedDate = selectedDate.minusMonths(1).withDayOfMonth(1);
            weekStart = selectedDate;
        } else {
            weekStart = weekStart.minusWeeks(1);
            selectedDate = weekStart;
        }
        displayedMonth = YearMonth.from(selectedDate);
        renderAll();
    }

    @FXML
    private void handleNextWeek() {
        if (currentView == CalendarView.DAILY) {
            selectedDate = selectedDate.plusDays(1);
            weekStart = selectedDate;
        } else if (currentView == CalendarView.MONTHLY) {
            selectedDate = selectedDate.plusMonths(1).withDayOfMonth(1);
            weekStart = selectedDate;
        } else {
            weekStart = weekStart.plusWeeks(1);
            selectedDate = weekStart;
        }
        displayedMonth = YearMonth.from(selectedDate);
        renderAll();
    }

    @FXML
    private void handleToday() {
        selectedDate = LocalDate.now();
        weekStart = selectedDate;
        displayedMonth = YearMonth.from(selectedDate);
        renderAll();
    }

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/add-task-modal.fxml"));
            StackPane overlay = loader.load();
            AddTaskModalController controller = loader.getController();
            controller.configure(taskDAO, () -> {
                Task newTask = controller.getCreatedTask();
                if (newTask != null) {
                    selectedDate = newTask.getDeadline() == null ? selectedDate : newTask.getDeadline();
                    weekStart = selectedDate;
                    displayedMonth = YearMonth.from(selectedDate);
                }
                selectedTags.addAll(getTaskTags());
                renderAll();
            });
            calendarRoot.getChildren().add(overlay);
            controller.requestInitialFocus();
        } catch (Exception e) {
            System.out.println("Could not load the Add Task window!");
            e.printStackTrace();
        }
    }

    private void renderAll() {
        renderViewButtons();
        renderModeButtons();
        renderMiniCalendar();
        renderFilters();
        renderSelectedDateTasks();
        renderCalendar();
    }

    private void renderViewButtons() {
        setViewButtonActive(dailyViewButton, currentView == CalendarView.DAILY);
        setViewButtonActive(weeklyViewButton, currentView == CalendarView.WEEKLY);
        setViewButtonActive(monthlyViewButton, currentView == CalendarView.MONTHLY);
    }

    private void setViewButtonActive(Button button, boolean active) {
        button.getStyleClass().removeAll("view-toggle-btn", "view-toggle-btn-active");
        button.getStyleClass().add(active ? "view-toggle-btn-active" : "view-toggle-btn");
    }

    private void renderModeButtons() {
        setModeButtonActive(frontloadButton, scheduleMode == ScheduleMode.FRONTLOAD);
        setModeButtonActive(backloadButton, scheduleMode == ScheduleMode.BACKLOAD);
    }

    private void setModeButtonActive(Button button, boolean active) {
        button.getStyleClass().removeAll("mode-toggle-btn", "mode-toggle-btn-active");
        button.getStyleClass().add(active ? "mode-toggle-btn-active" : "mode-toggle-btn");
    }

    private void renderMiniCalendar() {
        miniMonthLabel.setText(displayedMonth.format(miniMonthFormatter));
        miniMonthGrid.getChildren().clear();
        miniMonthGrid.getColumnConstraints().clear();
        miniMonthGrid.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7.0);
            column.setHalignment(HPos.CENTER);
            miniMonthGrid.getColumnConstraints().add(column);
        }

        String[] weekdays = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < weekdays.length; i++) {
            Label label = new Label(weekdays[i]);
            label.getStyleClass().add("mini-weekday");
            label.setMaxWidth(Double.MAX_VALUE);
            miniMonthGrid.add(label, i, 0);
        }

        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int firstColumn = firstOfMonth.getDayOfWeek().getValue() % 7;
        Set<LocalDate> taskDates = getTaskDates();

        for (int dayOfMonth = 1; dayOfMonth <= displayedMonth.lengthOfMonth(); dayOfMonth++) {
            LocalDate date = displayedMonth.atDay(dayOfMonth);
            int slot = firstColumn + dayOfMonth - 1;
            VBox cell = new VBox(2);
            cell.setAlignment(Pos.CENTER);
            cell.getStyleClass().add("mini-day-cell");
            if (date.equals(LocalDate.now())) {
                cell.getStyleClass().add("mini-day-today");
            }
            if (date.equals(selectedDate)) {
                cell.getStyleClass().add("mini-day-selected");
            }

            Label day = new Label(String.valueOf(date.getDayOfMonth()));
            day.getStyleClass().add("mini-day-number");
            cell.getChildren().add(day);

            if (taskDates.contains(date)) {
                Label dot = new Label();
                dot.getStyleClass().add("mini-task-dot");
                cell.getChildren().add(dot);
            }

            cell.setOnMouseClicked(event -> {
                selectedDate = date;
                weekStart = date;
                displayedMonth = YearMonth.from(date);
                renderAll();
            });
            miniMonthGrid.add(cell, slot % 7, (slot / 7) + 1);
        }
    }

    private void renderFilters() {
        filterList.getChildren().clear();
        Set<String> tags = getTaskTags();
        selectedTags.retainAll(tags);
        if (selectedTags.isEmpty()) {
            selectedTags.addAll(tags);
        }

        if (tags.isEmpty()) {
            Label empty = new Label("No task labels yet");
            empty.getStyleClass().add("empty-sidebar-label");
            filterList.getChildren().add(empty);
            return;
        }

        for (String tag : tags) {
            CheckBox checkBox = new CheckBox(capitalize(tag));
            checkBox.getStyleClass().add("filter-check");
            checkBox.setSelected(selectedTags.contains(tag));
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    selectedTags.add(tag);
                } else {
                    selectedTags.remove(tag);
                }
                renderSelectedDateTasks();
                renderCalendar();
                renderMiniCalendar();
            });
            filterList.getChildren().add(checkBox);
        }
    }

    private void renderSelectedDateTasks() {
        selectedDateLabel.setText(selectedDate.format(selectedDateFormatter));
        selectedDayTaskList.getChildren().clear();

        List<Task> tasks = getTasksForDate(selectedDate);
        if (tasks.isEmpty()) {
            Label empty = new Label("No scheduled tasks for this date");
            empty.getStyleClass().add("empty-sidebar-label");
            selectedDayTaskList.getChildren().add(empty);
            return;
        }

        for (Task task : tasks) {
            VBox card = new VBox(3);
            card.getStyleClass().add("selected-task-card");

            Label name = new Label(task.getName());
            name.getStyleClass().add("selected-task-name");
            name.setWrapText(true);

            Label meta = new Label(capitalize(task.getTag()) + "  |  " + String.format("%.1f hrs", task.getEstimatedTime()));
            meta.getStyleClass().add("selected-task-meta");

            card.getChildren().addAll(name, meta);
            selectedDayTaskList.getChildren().add(card);
        }
    }

    private void renderCalendar() {
        calculateDynamicGridHours();
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        if (currentView == CalendarView.MONTHLY) {
            renderMonthlyCalendar();
            return;
        }

        int visibleDays = currentView == CalendarView.DAILY ? 1 : 7;
        LocalDate rangeStart = currentView == CalendarView.DAILY ? selectedDate : weekStart;
        LocalDate rangeEnd = rangeStart.plusDays(visibleDays - 1);
        headerDateLabel.setText(currentView == CalendarView.DAILY
                ? selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                : rangeStart.format(headerFormatter) + " - " + rangeEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));

        configureTimedGrid(visibleDays);
        addHeaderRow(visibleDays);
        addTimeRows(visibleDays);
        addScheduledTasks(visibleDays);
    }

    private void configureTimedGrid(int visibleDays) {
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setPercentWidth(currentView == CalendarView.DAILY ? 14 : 8);
        timeColumn.setHalignment(HPos.RIGHT);
        calendarGrid.getColumnConstraints().add(timeColumn);

        double dayWidth = (100.0 - timeColumn.getPercentWidth()) / visibleDays;
        for (int i = 0; i < visibleDays; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setPercentWidth(dayWidth);
            dayColumn.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(dayColumn);
        }

        RowConstraints headerRow = new RowConstraints(58);
        headerRow.setMinHeight(58);
        calendarGrid.getRowConstraints().add(headerRow);

        for (int hour = dynamicStartHour; hour < dynamicEndHour; hour++) {
            RowConstraints hourRow = new RowConstraints(55);
            hourRow.setMinHeight(55);
            hourRow.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(hourRow);
        }
    }

    private void addHeaderRow(int visibleDays) {
        Label corner = new Label("GMT+8");
        corner.getStyleClass().add("corner-cell");
        corner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        calendarGrid.add(corner, 0, 0);

        for (int i = 0; i < visibleDays; i++) {
            LocalDate date = getVisibleDate(i);
            VBox header = new VBox(2);
            header.setAlignment(Pos.CENTER);
            header.getStyleClass().add("day-header");
            if (date.equals(selectedDate)) {
                header.getStyleClass().add("day-header-selected");
            }
            header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Label dayName = new Label(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            dayName.getStyleClass().add("day-name");

            Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
            dayNumber.getStyleClass().add(date.equals(LocalDate.now()) ? "day-number-today" : "day-number");

            header.getChildren().addAll(dayName, dayNumber);
            calendarGrid.add(header, i + 1, 0);
        }
    }

    private void addTimeRows(int visibleDays) {
        for (int hour = dynamicStartHour; hour < dynamicEndHour; hour++) {
            int row = (hour - dynamicStartHour) + 1;

            Label time = new Label(formatHour(hour));
            time.getStyleClass().add("time-label");
            time.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            calendarGrid.add(time, 0, row);

            for (int day = 0; day < visibleDays; day++) {
                VBox cell = new VBox(4);
                cell.getStyleClass().add("calendar-cell");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                GridPane.setHgrow(cell, Priority.ALWAYS);
                calendarGrid.add(cell, day + 1, row);
            }
        }
    }

    private void addScheduledTasks(int visibleDays) {
        List<TimeBlock> savedSchedule = taskService.getSavedSchedule();
        if (savedSchedule == null) return;

        for (int day = 0; day < visibleDays; day++) {
            LocalDate date = getVisibleDate(day);

            TimeBlock todayBlock = savedSchedule.stream()
                    .filter(b -> b.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (todayBlock == null || todayBlock.getAssignedTasks().isEmpty()) continue;

            int startingHour = todayBlock.getStartTime().getHour();
            int startingRow = (startingHour - dynamicStartHour) + 1;

            int taskOffset = 0;

            for (Task task : todayBlock.getAssignedTasks()) {
                if (!isTagVisible(task)) continue;

                double allocatedHours = todayBlock.getAllocatedHours(task.getId());

                int rowSpan = (int) Math.max(1, Math.round(allocatedHours));

                int targetRow = startingRow + taskOffset;
                if (targetRow > (dynamicEndHour - dynamicStartHour)) break;

                VBox taskPill = new VBox(2);
                taskPill.getStyleClass().add("task-pill-container");
                taskPill.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Label titleLabel = new Label(task.getName());
                titleLabel.getStyleClass().add("task-pill-title");
                titleLabel.setWrapText(true);

                Label metaLabel = new Label(capitalize(task.getTag()) + " | " + String.format("%.1fh", allocatedHours));
                metaLabel.getStyleClass().add("task-pill-meta");
                metaLabel.setWrapText(true);

                taskPill.getChildren().addAll(titleLabel, metaLabel);

                calendarGrid.add(taskPill, day + 1, targetRow);
                GridPane.setRowSpan(taskPill, rowSpan);

                GridPane.setMargin(taskPill, new Insets(2, 4, 2, 4));

                taskOffset += rowSpan;
            }
        }
    }

    private void renderMonthlyCalendar() {
        displayedMonth = YearMonth.from(selectedDate);
        headerDateLabel.setText(displayedMonth.format(miniMonthFormatter));

        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7.0);
            column.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(column);
        }

        RowConstraints headerRow = new RowConstraints(42);
        calendarGrid.getRowConstraints().add(headerRow);
        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints(86);
            row.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(row);
        }

        String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (int i = 0; i < weekdays.length; i++) {
            Label label = new Label(weekdays[i]);
            label.getStyleClass().add("month-weekday");
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            calendarGrid.add(label, i, 0);
        }

        int firstColumn = displayedMonth.atDay(1).getDayOfWeek().getValue() % 7;
        Map<LocalDate, List<Task>> tasksByDate = getVisibleTasksByDate();
        for (int dayOfMonth = 1; dayOfMonth <= displayedMonth.lengthOfMonth(); dayOfMonth++) {
            LocalDate date = displayedMonth.atDay(dayOfMonth);
            int slot = firstColumn + dayOfMonth - 1;
            VBox cell = new VBox(5);
            cell.getStyleClass().add("month-cell");
            if (date.equals(selectedDate)) {
                cell.getStyleClass().add("month-cell-selected");
            }
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            cell.setOnMouseClicked(event -> {
                selectedDate = date;
                weekStart = date;
                renderAll();
            });

            Label dayLabel = new Label(String.valueOf(dayOfMonth));
            dayLabel.getStyleClass().add(date.equals(LocalDate.now()) ? "month-day-today" : "month-day-number");
            cell.getChildren().add(dayLabel);

            for (Task task : tasksByDate.getOrDefault(date, List.of()).stream().limit(2).toList()) {
                Label taskLabel = new Label(task.getName());
                taskLabel.getStyleClass().add("month-task-pill");
                taskLabel.setMaxWidth(Double.MAX_VALUE);
                cell.getChildren().add(taskLabel);
            }

            calendarGrid.add(cell, slot % 7, (slot / 7) + 1);
        }
    }

    private LocalDate getVisibleDate(int offset) {
        return currentView == CalendarView.DAILY ? selectedDate : weekStart.plusDays(offset);
    }

    private Map<LocalDate, List<Task>> getVisibleTasksByDate() {
        Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();
        List<TimeBlock> savedSchedule = taskService.getSavedSchedule();
        if (savedSchedule != null) {
            for (TimeBlock block : savedSchedule) {
                if (block.getDate() == null) {
                    continue;
                }
                for (Task task : block.getAssignedTasks()) {
                    if (isTagVisible(task)) {
                        List<Task> tasks = tasksByDate.computeIfAbsent(block.getDate(), ignored -> new ArrayList<>());
                        if (tasks.stream().noneMatch(existing -> existing.getId().equals(task.getId()))) {
                            tasks.add(task);
                        }
                    }
                }
            }
        }

        tasksByDate.values().forEach(tasks -> tasks.sort(Comparator.comparing(Task::getPriorityScore).reversed()));
        return tasksByDate;
    }

    private List<Task> getTasksForDate(LocalDate date) {
        return getVisibleTasksByDate().getOrDefault(date, List.of());
    }

    private Set<LocalDate> getTaskDates() {
        return getVisibleTasksByDate().keySet();
    }

    private Set<String> getTaskTags() {
        Set<String> tags = new LinkedHashSet<>();
        for (Task task : getAllTasks()) {
            tags.add(normalizeTag(task.getTag()));
        }
        return tags;
    }

    private List<Task> getAllTasks() {
        if (taskDAO == null || taskDAO.getAllTasks() == null) {
            return List.of();
        }
        return taskDAO.getAllTasks();
    }

    private boolean isTagVisible(Task task) {
        return selectedTags.contains(normalizeTag(task.getTag()));
    }

    private VBox getCell(int column, int row) {
        return calendarGrid.getChildren()
                .stream()
                .filter(node -> GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == column)
                .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row)
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .findFirst()
                .orElse(null);
    }

    private String normalizeTag(String tag) {
        return tag == null || tag.isBlank() ? "work" : tag.trim().toLowerCase(Locale.ROOT);
    }

    private String capitalize(String text) {
        String normalized = normalizeTag(text);
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private String formatHour(int hour) {
        int displayHour = hour % 12 == 0 ? 12 : hour % 12;
        String suffix = hour < 12 ? "AM" : "PM";
        return displayHour + " " + suffix;
    }

    private void promptForWeeklyAvailabilityIfDue() {
        LocalDate today = SystemClock.today();
        boolean firstDayOfWeekAtEight = today.getDayOfWeek() == DayOfWeek.MONDAY && !SystemClock.now().isBefore(java.time.LocalTime.of(8, 0));
        LocalDate scheduleStart = startOfWeek(today);
        if (firstDayOfWeekAtEight && !scheduleStart.equals(lastAvailabilityPromptWeekStart) && !hasScheduleForWeek(scheduleStart)) {
            lastAvailabilityPromptWeekStart = scheduleStart;
            showAvailabilityDialog(scheduleStart);
        }
    }

    private void startWeeklyAvailabilityWatcher() {
        availabilityWatcher = new Timeline(new KeyFrame(Duration.minutes(1), event -> promptForWeeklyAvailabilityIfDue()));
        availabilityWatcher.setCycleCount(Timeline.INDEFINITE);
        availabilityWatcher.play();
    }

    private boolean hasScheduleForWeek(LocalDate scheduleStart) {
        List<TimeBlock> schedule = taskService.getSavedSchedule();
        if (schedule == null || schedule.size() != 7) {
            return false;
        }
        return schedule.stream().anyMatch(block -> scheduleStart.equals(block.getDate()));
    }
    private boolean showAvailabilityDialog(LocalDate scheduleStart) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Weekly Availability");
        dialog.setHeaderText("Select your free time window for each day:");
        if (calendarGrid.getScene() != null) {
            dialog.initOwner(calendarGrid.getScene().getWindow());
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(12));

        List<javafx.scene.control.ComboBox<Integer>> startCombos = new ArrayList<>();
        List<javafx.scene.control.ComboBox<Integer>> endCombos = new ArrayList<>();

        List<TimeBlock> savedSchedule = taskService.getSavedSchedule();

        for (int i = 0; i < 7; i++) {
            LocalDate date = scheduleStart.plusDays(i);
            Label label = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + date.format(DateTimeFormatter.ofPattern("MMM d")));

            TimeBlock savedDay = null;
            if (savedSchedule != null) {
                savedDay = savedSchedule.stream()
                        .filter(b -> b.getDate() != null && b.getDate().equals(date))
                        .findFirst()
                        .orElse(null);
            }

            javafx.scene.control.ComboBox<Integer> startCombo = new javafx.scene.control.ComboBox<>();
            for (int h = 8; h < 22; h++) startCombo.getItems().add(h);

            javafx.scene.control.ComboBox<Integer> endCombo = new javafx.scene.control.ComboBox<>();
            for (int h = 9; h <= 22; h++) endCombo.getItems().add(h);

            if (savedDay != null && savedDay.getStartTime() != null && savedDay.getTotalAvailableHours() > 0) {
                startCombo.setValue(savedDay.getStartTime().getHour());
                endCombo.setValue(savedDay.getEndTime().getHour());
            } else {
                startCombo.setPromptText("Start");
                endCombo.setPromptText("End");
            }

            HBox timeRow = new HBox(8, startCombo, new Label("to"), endCombo);
            timeRow.setAlignment(Pos.CENTER_LEFT);

            form.add(label, 0, i);
            form.add(timeRow, 1, i);

            startCombos.add(startCombo);
            endCombos.add(endCombo);
        }

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return false;
        }

        List<TimeBlock> week = new ArrayList<>();
        try {
            for (int i = 0; i < 7; i++) {
                Integer start = startCombos.get(i).getValue();
                Integer end = endCombos.get(i).getValue();

                double hours = 0.0;
                java.time.LocalTime startTime = java.time.LocalTime.of(8, 0);
                java.time.LocalTime endTime = java.time.LocalTime.of(22, 0);

                if (start != null && end != null) {
                    if (start >= end) {
                        throw new InvalidTaskException("End time must be after Start time!");
                    }
                    hours = (double) (end - start);
                    startTime = java.time.LocalTime.of(start, 0);
                    endTime = java.time.LocalTime.of(end, 0);
                }

                week.add(new TimeBlock(scheduleStart.plusDays(i), startTime, endTime, hours));
            }
            taskService.createWeeklySchedule(week);
            renderAll();
            return true;
        } catch (InvalidTaskException e) {
            showError("Invalid Time Selection", e.getMessage());
        }
        return false;
    }

    private Map<LocalDate, Double> getAvailabilityByDate() {
        Map<LocalDate, Double> availability = new HashMap<>();
        List<TimeBlock> schedule = taskService.getSavedSchedule();
        if (schedule != null) {
            for (TimeBlock block : schedule) {
                if (block.getDate() != null) {
                    availability.put(block.getDate(), block.getTotalAvailableHours());
                }
            }
        }
        return availability;
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    private LocalDate getSchedulingWeekStart(LocalDate date) {
        LocalDate monday = startOfWeek(date);
        return monday.isBefore(SystemClock.today()) ? SystemClock.today() : monday;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (calendarGrid.getScene() != null) {
            alert.initOwner(calendarGrid.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message == null || message.isBlank() ? "Please try again." : message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (calendarGrid.getScene() != null) {
            alert.initOwner(calendarGrid.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private void calculateDynamicGridHours() {
        List<TimeBlock> schedule = taskService.getSavedSchedule();

        if (schedule == null || schedule.isEmpty()) {
            dynamicStartHour = 8;
            dynamicEndHour = 22;
            return;
        }

        int minStart = 24;
        int maxEnd = 0;
        boolean hasValidBlocks = false;

        for (TimeBlock block : schedule) {
            if (block.getStartTime() != null && block.getEndTime() != null) {
                minStart = Math.min(minStart, block.getStartTime().getHour());
                maxEnd = Math.max(maxEnd, block.getEndTime().getHour());
                hasValidBlocks = true;
            }
        }

        if (hasValidBlocks) {
            dynamicStartHour = minStart;
            dynamicEndHour = maxEnd;
        } else {
            dynamicStartHour = 8;
            dynamicEndHour = 22;
        }
    }
}
