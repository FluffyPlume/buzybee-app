package com.buzybee.app.controller;

import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.model.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AddTaskModalController {

    @FXML private StackPane overlayRoot;
    @FXML private TextField nameField;
    @FXML private TextField labelField;
    @FXML private Button difficultyButton;
    @FXML private ScrollPane difficultyScroll;
    @FXML private VBox difficultyPanel;

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

    private int selectedDifficulty;
    private double selectedHours;
    private LocalDate selectedDate = LocalDate.now();
    private YearMonth pickerMonth = YearMonth.now();
    private DropdownPopup difficultyPopup;
    private DropdownPopup datePopup;
    private DropdownPopup timePopup;

    private int timeHour = 0;
    private int timeMin  = 0;
    private int timeSec  = 0;

    private Task taskToEdit = null;
    private Task createdTask = null;

    private TaskDAO taskDAO;
    private Runnable onTaskSaved = () -> { };

    @FXML
    private void initialize() {
        // Use Platform.runLater so the scene is fully built before we
        // pull nodes out of it — this is the key fix when managed=false
        // causes getParent() to return null during initialize()
        Platform.runLater(() -> {
            difficultyPopup = createScrollableDropdownPopup(difficultyScroll, difficultyPanel, 120);
            datePopup       = createDropdownPopup(datePanel, 300);
            timePopup       = createDropdownPopup(timePanel, 240);
        });

        styleDropdownButton(difficultyButton, "Difficulty");
        styleDropdownButton(timeButton, "Estimated Time");
        styleDropdownButton(dateButton, "Deadline");

        populateDifficultyOptions();
        renderCalendar();
        initTimeFields();
    }

    // ── Arrow Graphic Helper ──────────────────────────────────────────────────

    private Polygon createArrow() {
        Polygon arrow = new Polygon(0.0, 0.0, 10.0, 0.0, 5.0, 6.0);
        arrow.setFill(Color.web("#FAEEDA"));
        arrow.setStroke(Color.web("#483521"));
        arrow.setStrokeWidth(1.5);
        return arrow;
    }

    private void styleDropdownButton(Button button, String label) {
        javafx.scene.layout.HBox wrapper = new javafx.scene.layout.HBox();
        wrapper.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label textLabel = new Label(label);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.HBox.setHgrow(textLabel, javafx.scene.layout.Priority.ALWAYS);
        textLabel.getStyleClass().add("dropdown-trigger-label");

        wrapper.getChildren().addAll(textLabel, createArrow());

        button.setText("");
        button.setGraphic(wrapper);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setMaxWidth(Double.MAX_VALUE);

        button.widthProperty().addListener((obs, oldVal, newVal) -> {
            double padding = 28.0;
            wrapper.setPrefWidth(newVal.doubleValue() - padding);
        });
    }

    private void updateButtonLabel(Button button, String text) {
        if (button.getGraphic() instanceof javafx.scene.layout.HBox wrapper) {
            wrapper.getChildren().stream()
                    .filter(n -> n instanceof Label)
                    .map(n -> (Label) n)
                    .findFirst()
                    .ifPresent(lbl -> lbl.setText(text));
        }
    }

    public void setTaskToEdit(Task task) {
        this.taskToEdit = task;

        nameField.setText(task.getName());
        labelField.setText(task.getTag());

        selectedDifficulty = (int) task.getDifficulty();
        updateButtonLabel(difficultyButton, String.valueOf(selectedDifficulty));

        selectedDate = task.getDeadline();
        pickerMonth = YearMonth.from(selectedDate);
        updateButtonLabel(dateButton, selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        renderCalendar();

        double t = task.getEstimatedTime();
        timeHour = (int) t;
        timeMin = (int) ((t - timeHour) * 60);
        timeSec = (int) ((((t - timeHour) * 60) - timeMin) * 60);

        hourField.setText(String.format("%02d", timeHour));
        minField.setText(String.format("%02d", timeMin));
        secField.setText(String.format("%02d", timeSec));
        syncTimeButton();
    }

    public Task getCreatedTask() {
        return createdTask;
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
        field.setOnMouseClicked(e -> field.selectAll());
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                Platform.runLater(field::selectAll);
            } else {
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
            updateButtonLabel(timeButton, "Estimated Time");
        } else {
            updateButtonLabel(timeButton, String.format("%02d:%02d:%02d", timeHour, timeMin, timeSec));
        }
    }

    private double getSelectedHoursFromSpinner() {
        return timeHour + timeMin / 60.0 + timeSec / 3600.0;
    }

    // ── Dropdowns ─────────────────────────────────────────────────────────────

    @FXML private void toggleDifficultyDropdown() { toggleDropdown(difficultyButton, difficultyPopup); }
    @FXML private void toggleDateDropdown()        { toggleDropdown(dateButton,       datePopup);       }
    @FXML private void toggleTimeDropdown()        { toggleDropdown(timeButton,       timePopup);       }

    @FXML private void showPreviousMonth() { pickerMonth = pickerMonth.minusMonths(1); renderCalendar(); }
    @FXML private void showNextMonth()     { pickerMonth = pickerMonth.plusMonths(1);  renderCalendar(); }

    @FXML
    private void handleCancel() { closeWindow(); }

    @FXML
    private void handleAddTask() {
        String name = textValue(nameField);
        String tag  = textValue(labelField);

        if (name.isEmpty()) { statusLabel.setText("Task name is required."); return; }
        if (selectedDate == null) { statusLabel.setText("Due date is required."); return; }

        selectedHours = getSelectedHoursFromSpinner();
        if (selectedHours <= 0) { statusLabel.setText("Estimated time must be greater than zero."); return; }
        if (selectedDifficulty <= 0) { statusLabel.setText("Difficulty is required."); return; }

        if (taskToEdit == null) {
            createdTask = new Task(name, selectedDifficulty, selectedHours, selectedDate);
            createdTask.setTag(tag.isEmpty() ? "work" : tag.toLowerCase(Locale.ROOT));

            if (taskDAO != null) {
                taskDAO.getAllTasks().add(0, createdTask);
                taskDAO.saveChanges();
                onTaskSaved.run();
            }

        } else {
            taskToEdit.setName(name);
            taskToEdit.setDifficulty(selectedDifficulty);
            taskToEdit.setEstimatedTime(selectedHours);
            taskToEdit.setDeadline(selectedDate);
            taskToEdit.setTag(tag.isEmpty() ? "work" : tag.toLowerCase(Locale.ROOT));
            createdTask = taskToEdit;
        }

        closeWindow();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private DropdownPopup createScrollableDropdownPopup(ScrollPane scroll, VBox panel, double width) {
        javafx.scene.Parent scrollParent = scroll.getParent();
        if (scrollParent instanceof Pane p) { p.getChildren().remove(scroll); }

        // Strip border from the inner panel — border lives on outerWrapper below
        panel.getStyleClass().remove("dropdown-panel");
        panel.setStyle("-fx-background-color: transparent; -fx-padding: 2;");

        // Make the ScrollPane itself transparent so only outerWrapper paints the
        // rounded background/border — otherwise the ScrollPane's own square
        // background shows through and spills past the rounded corners.
        scroll.getStyleClass().add("dropdown-scroll-pane");
        scroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-border-color: transparent;");

        scroll.setVisible(true);
        scroll.setManaged(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        scroll.setMaxHeight(200);
        // Leave room inside the padded wrapper so the scrollbar doesn't sit on the border
        double innerWidth = width - 12;
        scroll.setMinWidth(innerWidth);
        scroll.setPrefWidth(innerWidth);
        scroll.setMaxWidth(innerWidth);

        // outerWrapper IS the popup root — border+radius+shadow render here
        VBox outerWrapper = new VBox(scroll);
        outerWrapper.getStyleClass().add("dropdown-panel");
        outerWrapper.setMinWidth(width);
        outerWrapper.setPrefWidth(width);
        outerWrapper.setMaxWidth(width);
        outerWrapper.setPadding(new Insets(6));

        // Clip to the rounded shape so the ScrollPane/scrollbar never spill
        // past outerWrapper's rounded border. Bound to actual size so it
        // always matches, regardless of padding/content size.
        Rectangle clip = new Rectangle();
        clip.setArcWidth(17);
        clip.setArcHeight(17);
        clip.widthProperty().bind(outerWrapper.widthProperty());
        clip.heightProperty().bind(outerWrapper.heightProperty());
        outerWrapper.setClip(clip);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(false);         // ← stops the popup repositioning that clips borders
        popup.getContent().add(outerWrapper);

        popup.setOnShown(e -> {
            // Make popup scene background transparent so shadow renders fully
            if (outerWrapper.getScene() != null) {
                outerWrapper.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            }
            javafx.scene.Scene popupScene = outerWrapper.getScene();
            if (popupScene != null) {
                String css = getClass().getResource("/com/buzybee/css/add-task-modal.css") != null
                        ? getClass().getResource("/com/buzybee/css/add-task-modal.css").toExternalForm()
                        : overlayRoot.getStylesheets().isEmpty() ? null : overlayRoot.getStylesheets().get(0);
                if (css != null && !popupScene.getStylesheets().contains(css)) {
                    popupScene.getStylesheets().add(css);
                }
            }
        });

        return new DropdownPopup(popup, scroll, outerWrapper, width);
    }

    private DropdownPopup createDropdownPopup(VBox panel, double width) {
        javafx.scene.Parent parent = panel.getParent();
        if (parent instanceof Pane p) { p.getChildren().remove(panel); }

        panel.setVisible(true);
        panel.setManaged(true);
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(false);
        popup.getContent().add(panel);

        popup.setOnShown(e -> {
            if (panel.getScene() != null) {
                panel.getScene().setFill(Color.TRANSPARENT);
            }
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
    if (dropdown == null) return;

    boolean show = !dropdown.popup.isShowing();
    hideDropdownPanels();
    if (show) {
        dropdown.panel.applyCss();
        dropdown.panel.layout();
        double actualWidth = dropdown.panel.getWidth() > 0 ? dropdown.panel.getWidth() : dropdown.width;
        Point2D point = trigger.localToScreen(
                (trigger.getWidth() / 2) - (actualWidth / 2),
                trigger.getHeight() + 6
        );
        double maxHeight = availablePopupHeight(trigger, point.getY());
        dropdown.panel.setMaxHeight(Math.min(maxHeight, 220));
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
        for (int level = 1; level <= 10; level++) {
            int difficulty = level;
            Button option = createDropdownOption(String.valueOf(level));
            option.setOnAction(event -> {
                selectedDifficulty = difficulty;
                updateButtonLabel(difficultyButton, String.valueOf(difficulty));
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
                updateButtonLabel(dateButton, date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
                hideDropdownPanels();
                renderCalendar();
            });
            dateGrid.add(btn, cellIndex % 7, (cellIndex / 7) + 1);
        }
    }

    private void closeWindow() {
    hideDropdownPanels();
    if (overlayRoot.getParent() instanceof Pane parent) {
        parent.getChildren().remove(overlayRoot);
    } else {
        Stage stage = (Stage) overlayRoot.getScene().getWindow();
        stage.close();
    }
}

    private String textValue(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private record DropdownPopup(Popup popup, ScrollPane scrollPane, VBox panel, double width) { }

    public void configure(TaskDAO taskDAO, Runnable onTaskSaved) {
        this.taskDAO = taskDAO;
        this.onTaskSaved = onTaskSaved == null ? () -> { } : onTaskSaved;
    }

    public void requestInitialFocus() {
        Platform.runLater(nameField::requestFocus);
    }
}
