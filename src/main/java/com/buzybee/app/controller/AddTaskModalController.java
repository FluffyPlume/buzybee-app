package com.buzybee.app.controller;

import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AddTaskModalController {

    @FXML private StackPane overlayRoot;
    @FXML private TextField nameField;
    @FXML private TextField labelField;
    @FXML private Button difficultyButton;
    @FXML private VBox difficultyPanel;

    // Time picker controls
    @FXML private Button timeButton;
    @FXML private VBox timePanel;
    @FXML private TextField hourField;
    @FXML private TextField minField;
    @FXML private TextField secField;

    @FXML private Button dateButton;
    @FXML private VBox datePanel;
    @FXML private Label dateMonthLabel;
    @FXML private GridPane dateGrid;
    @FXML private Label statusLabel;

    private TaskDAO taskDAO;
    private Runnable onTaskSaved = () -> { };
    private int selectedDifficulty;
    private double selectedHours;
    private LocalDate selectedDate = LocalDate.now();
    private YearMonth pickerMonth = YearMonth.now();
    private DropdownPopup difficultyPopup;
    private DropdownPopup datePopup;
    private DropdownPopup timePopup;

    // Time spinner state
    private int timeHour = 0;
    private int timeMin  = 0;
    private int timeSec  = 0;

    @FXML
    private void initialize() {
        difficultyPopup = createDropdownPopup(difficultyPanel, 198);
        datePopup       = createDropdownPopup(datePanel, 300);
        timePopup       = createDropdownPopup(timePanel, 220);

        populateDifficultyOptions();
        renderCalendar();
        initTimeFields();
    }

    // ── Time Spinner ──────────────────────────────────────────────────────────

    private void initTimeFields() {
        setupTimeField(hourField, 0, 23, () -> timeHour, v -> { timeHour = v; syncTimeButton(); });
        setupTimeField(minField,  0, 59, () -> timeMin,  v -> { timeMin  = v; syncTimeButton(); });
        setupTimeField(secField,  0, 59, () -> timeSec,  v -> { timeSec  = v; syncTimeButton(); });
    }

    private void setupTimeField(TextField field, int min, int max,
                                java.util.function.IntSupplier getter,
                                java.util.function.IntConsumer setter) {
        // Click → select all so user types to replace
        field.setOnMouseClicked(e -> field.selectAll());
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                Platform.runLater(field::selectAll);
            } else {
                // Commit on focus loss
                commitTimeField(field, min, max, getter, setter);
            }
        });
        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                commitTimeField(field, min, max, getter, setter);
            } else if (e.getCode() == KeyCode.UP) {
                int next = (getter.getAsInt() + 1 > max) ? min : getter.getAsInt() + 1;
                setter.accept(next);
                field.setText(String.format("%02d", next));
                field.selectAll();
            } else if (e.getCode() == KeyCode.DOWN) {
                int prev = (getter.getAsInt() - 1 < min) ? max : getter.getAsInt() - 1;
                setter.accept(prev);
                field.setText(String.format("%02d", prev));
                field.selectAll();
            }
        });
    }

    private void commitTimeField(TextField field, int min, int max,
                                 java.util.function.IntSupplier getter,
                                 java.util.function.IntConsumer setter) {
        try {
            int val = Integer.parseInt(field.getText().trim());
            val = Math.max(min, Math.min(max, val));
            setter.accept(val);
            field.setText(String.format("%02d", val));
        } catch (NumberFormatException ex) {
            field.setText(String.format("%02d", getter.getAsInt()));
        }
    }

    @FXML private void hourUp()   { timeHour = (timeHour + 1 > 23) ? 0 : timeHour + 1; hourField.setText(String.format("%02d", timeHour)); syncTimeButton(); }
    @FXML private void hourDown() { timeHour = (timeHour - 1 < 0) ? 23 : timeHour - 1; hourField.setText(String.format("%02d", timeHour)); syncTimeButton(); }
    @FXML private void minUp()    { timeMin  = (timeMin  + 1 > 59) ? 0 : timeMin  + 1; minField.setText(String.format("%02d", timeMin));  syncTimeButton(); }
    @FXML private void minDown()  { timeMin  = (timeMin  - 1 < 0) ? 59 : timeMin  - 1; minField.setText(String.format("%02d", timeMin));  syncTimeButton(); }
    @FXML private void secUp()    { timeSec  = (timeSec  + 1 > 59) ? 0 : timeSec  + 1; secField.setText(String.format("%02d", timeSec));  syncTimeButton(); }
    @FXML private void secDown()  { timeSec  = (timeSec  - 1 < 0) ? 59 : timeSec  - 1; secField.setText(String.format("%02d", timeSec));  syncTimeButton(); }

    private void syncTimeButton() {
        if (timeHour == 0 && timeMin == 0 && timeSec == 0) {
            timeButton.setText("Estimated Time  \u25be");
        } else {
            timeButton.setText(String.format("%02d:%02d:%02d  \u25be", timeHour, timeMin, timeSec));
        }
    }

    private double getSelectedHoursFromSpinner() {
        return timeHour + timeMin / 60.0 + timeSec / 3600.0;
    }

    // ── Dropdowns ─────────────────────────────────────────────────────────────

    void configure(TaskDAO taskDAO, Runnable onTaskSaved) {
        this.taskDAO = taskDAO;
        this.onTaskSaved = onTaskSaved == null ? () -> { } : onTaskSaved;
    }

    void requestInitialFocus() {
        Platform.runLater(nameField::requestFocus);
    }

    @FXML private void toggleDifficultyDropdown() { toggleDropdown(difficultyButton, difficultyPopup); }
    @FXML private void toggleDateDropdown()        { toggleDropdown(dateButton,       datePopup);       }
    @FXML private void toggleTimeDropdown()        { toggleDropdown(timeButton,       timePopup);       }

    @FXML private void showPreviousMonth() { pickerMonth = pickerMonth.minusMonths(1); renderCalendar(); }
    @FXML private void showNextMonth()     { pickerMonth = pickerMonth.plusMonths(1);  renderCalendar(); }

    @FXML
    private void handleCancel() { close(); }

    @FXML
    private void handleAddTask() {
        String name = textValue(nameField);
        String tag  = textValue(labelField);

        if (name.isEmpty()) { statusLabel.setText("Task name is required."); return; }
        if (selectedDate == null) { statusLabel.setText("Due date is required."); return; }

        selectedHours = getSelectedHoursFromSpinner();
        if (selectedHours <= 0) { statusLabel.setText("Estimated time must be greater than zero."); return; }
        if (selectedDifficulty <= 0) { statusLabel.setText("Difficulty is required."); return; }

        Task task = new Task(name, selectedDifficulty, selectedHours, selectedDate);
        task.setTag(tag.isEmpty() ? "work" : tag.toLowerCase(Locale.ROOT));
        if (taskDAO != null) {
            taskDAO.getAllTasks().add(0, task);
            taskDAO.saveChanges();
        }
        onTaskSaved.run();
        close();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private DropdownPopup createDropdownPopup(VBox panel, double width) {
        Pane parent = (Pane) panel.getParent();
        if (parent != null) { parent.getChildren().remove(panel); }
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(panel);

        // Inject the stylesheet into the popup's own scene so that
        // .dropdown-panel (background, border, radius) renders correctly.
        popup.setOnShown(e -> {
            javafx.scene.Scene popupScene = panel.getScene();
            if (popupScene != null) {
                String css = getClass().getResource("/com/buzybee/css/add-task-modal.css") != null
                        ? getClass().getResource("/com/buzybee/css/add-task-modal.css").toExternalForm()
                        : overlayRoot.getStylesheets().isEmpty() ? null : overlayRoot.getStylesheets().get(0);
                if (css != null && !popupScene.getStylesheets().contains(css)) {
                    popupScene.getStylesheets().add(css);
                }
            }
        });

        return new DropdownPopup(popup, null, panel, width);
    }

    private void toggleDropdown(Button trigger, DropdownPopup dropdown) {
        boolean show = !dropdown.popup.isShowing();
        hideDropdownPanels();
        if (show) {
            dropdown.panel.applyCss();
            dropdown.panel.layout();
            Point2D point = trigger.localToScreen(
                    (trigger.getWidth() - dropdown.width) / 2,
                    trigger.getHeight() + 6
            );
            double maxHeight = availablePopupHeight(trigger, point.getY());
            dropdown.panel.setMaxHeight(maxHeight);
            dropdown.popup.show(trigger, point.getX(), point.getY());
        }
    }

    private double availablePopupHeight(Button trigger, double popupY) {
        Window window = trigger.getScene().getWindow();
        double windowBottom = window.getY() + window.getHeight();
        return Math.max(72, windowBottom - popupY - 14);
    }

    private void hideDropdownPanels() {
        if (difficultyPopup != null) difficultyPopup.popup.hide();
        if (datePopup       != null) datePopup.popup.hide();
        if (timePopup       != null) timePopup.popup.hide();
    }

    private void populateDifficultyOptions() {
        difficultyPanel.getChildren().clear();
        for (int level = 1; level <= 5; level++) {
            int difficulty = level;
            Button option = createDropdownOption(String.valueOf(level));
            option.setOnAction(event -> {
                selectedDifficulty = difficulty;
                difficultyButton.setText(difficulty + "  \u25be");
                hideDropdownPanels();
            });
            difficultyPanel.getChildren().add(option);
        }
    }

    private Button createDropdownOption(String text) {
        Button option = new Button(text);
        option.setMaxWidth(Double.MAX_VALUE);
        option.getStyleClass().add("dropdown-option");
        return option;
    }

    private void renderCalendar() {
        dateMonthLabel.setText(pickerMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        dateGrid.getChildren().clear();
        dateGrid.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(34); col.setPrefWidth(34);
            dateGrid.getColumnConstraints().add(col);
        }
        String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < weekdays.length; i++) {
            Label wd = new Label(weekdays[i]);
            wd.getStyleClass().add(i == 0 || i == 6 ? "dropdown-weekday-edge" : "dropdown-weekday");
            dateGrid.add(wd, i, 0);
        }
        LocalDate firstDay = pickerMonth.atDay(1);
        int startColumn = firstDay.getDayOfWeek().getValue() % 7;
        for (int day = 1; day <= pickerMonth.lengthOfMonth(); day++) {
            LocalDate date = pickerMonth.atDay(day);
            int cellIndex = startColumn + day - 1;
            Button btn = new Button(String.valueOf(date.getDayOfMonth()));
            btn.getStyleClass().add("dropdown-day");
            if (date.equals(selectedDate)) btn.getStyleClass().add("dropdown-day-selected");
            btn.setOnAction(e -> {
                selectedDate = date;
                dateButton.setText(date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + "  \u25be");
                hideDropdownPanels();
                renderCalendar();
            });
            dateGrid.add(btn, cellIndex % 7, (cellIndex / 7) + 1);
        }
    }

    private void close() {
        hideDropdownPanels();
        Pane parent = (Pane) overlayRoot.getParent();
        if (parent != null) parent.getChildren().remove(overlayRoot);
    }

    private String textValue(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private record DropdownPopup(Popup popup, ScrollPane scrollPane, VBox panel, double width) { }
}