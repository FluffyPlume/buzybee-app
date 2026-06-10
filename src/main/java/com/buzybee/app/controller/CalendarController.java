package com.buzybee.app.controller;

import com.buzybee.app.exception.InvalidTaskException;
import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarController {

    @FXML private DatePicker dpStartDate;
    @FXML private ComboBox<String> cmbScheduleMode; // "Frontload" or "Backload"
    @FXML private Label lblSystemMessage; // For success/error messages

    @FXML private TextField txtMonTime;
    @FXML private TextField txtTueTime;
    @FXML private TextField txtWedTime;
    @FXML private TextField txtThuTime;
    @FXML private TextField txtFriTime;
    @FXML private TextField txtSatTime;
    @FXML private TextField txtSunTime;

    // The Output View
    @FXML private ListView<String> calendarListView;

    private TaskService taskService;
    private ObservableList<String> calendarDisplayData = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        taskService = new TaskService();

        // Setup the Dropdown Options
        cmbScheduleMode.setItems(FXCollections.observableArrayList("Frontload", "Backload"));
        cmbScheduleMode.getSelectionModel().selectFirst(); // Default to Frontload

        // Set default start date to today
        dpStartDate.setValue(LocalDate.now());

        calendarListView.setItems(calendarDisplayData);
    }

    // Trigger Buttons
    @FXML
    private void handleGenerateSchedule() {
        lblSystemMessage.setText(""); // Clear old messages

        try {
            LocalDate startDate = dpStartDate.getValue();
            String mode = cmbScheduleMode.getValue();

            if (startDate == null) {
                lblSystemMessage.setText("Error: Please select a Start Date.");
                return;
            }

            // Gather the daily free time capacities
            List<TimeBlock> newWeekTemplate = buildWeeklyTemplate(startDate);

            // Save the empty week to the database so the algorithm has a container
            taskService.createWeeklySchedule(newWeekTemplate);

            // Trigger the Greedy Algorithm to fill those empty containers!
            taskService.generateWeeklySchedule(mode);

            //  Render the results to the UI
            refreshCalendarView();

            lblSystemMessage.setText("Success: Schedule Generated using " + mode + " strategy!");

        } catch (NumberFormatException e) {
            lblSystemMessage.setText("Error: Please enter valid numbers for daily free hours.");
        } catch (InvalidTaskException e) {
            lblSystemMessage.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            lblSystemMessage.setText("An unexpected system error occurred.");
            e.printStackTrace();
        }
    }


    // Extracts the numbers from the 7 text fields and builds the week
    private List<TimeBlock> buildWeeklyTemplate(LocalDate startDate) throws NumberFormatException {
        List<TimeBlock> week = new ArrayList<>();

        // Ensure empty fields default to 0.0 to prevent crashes
        double mon = parseHours(txtMonTime.getText());
        double tue = parseHours(txtTueTime.getText());
        double wed = parseHours(txtWedTime.getText());
        double thu = parseHours(txtThuTime.getText());
        double fri = parseHours(txtFriTime.getText());
        double sat = parseHours(txtSatTime.getText());
        double sun = parseHours(txtSunTime.getText());

        // Create the 7 days sequentially
        week.add(new TimeBlock(startDate, mon));
        week.add(new TimeBlock(startDate.plusDays(1), tue));
        week.add(new TimeBlock(startDate.plusDays(2), wed));
        week.add(new TimeBlock(startDate.plusDays(3), thu));
        week.add(new TimeBlock(startDate.plusDays(4), fri));
        week.add(new TimeBlock(startDate.plusDays(5), sat));
        week.add(new TimeBlock(startDate.plusDays(6), sun));

        return week;
    }

    private double parseHours(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) return 0.0;
        return Double.parseDouble(text.trim());
    }

    private void refreshCalendarView() {
        calendarDisplayData.clear();

        // Fetch the weekly template from the database
        List<TimeBlock> savedWeek = taskService.getSavedSchedule();

        if (savedWeek == null || savedWeek.isEmpty()) {
            calendarDisplayData.add("No schedule generated yet. Enter your free time and click Generate!");
            return;
        }

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd");

        // Loop through all 7 days and build the UI text
        for (TimeBlock day : savedWeek) {

            // Add the Day Header showing total capacity vs remaining free time
            String dayHeader = day.getDate().format(formatter) +
                    "  —  [Capacity: " + day.getTotalAvailableHours() +
                    " hrs | Remaining: " + day.getRemainingHours() + " hrs]";
            calendarDisplayData.add(dayHeader);

            //  Because your Greedy Algorithm is smart, we just ask the block for its tasks!
            List<Task> tasksForToday = day.getAssignedTasks();

            if (tasksForToday.isEmpty()) {
                calendarDisplayData.add("   [No tasks scheduled for this day]");
            } else {
                for (Task t : tasksForToday) {
                    // Add the assigned tasks underneath the day
                    calendarDisplayData.add(String.format("   ▶ %s (%.1f hrs) | Score: %.2f | Status: %s",
                            t.getName(), t.getEstimatedTime(), t.getPriorityScore(), t.getStatus()));
                }
            }

            // Add a blank line for visual spacing between days
            calendarDisplayData.add("");
        }
    }
}