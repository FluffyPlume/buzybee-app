package com.buzybee.app.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


/**
 * Represents an available scheduling window.
 * Tracks the total available hours and remaining capacity for
 * the Greedy Task Scheduling algorithm.
 */
public class TimeBlock {

    private String id;
    private LocalDate date;
    private LocalTime startTime; // e.g., 4:00 PM
    private LocalTime endTime; // e.g., 8:00 PM

    private double totalAvailableHours; // e.g., 3.0
    private double remainingHours;
    private List<Task> assignedTasks = new java.util.ArrayList<>();

    // Constructor
    public TimeBlock(LocalDate date, LocalTime startTime, LocalTime endTime, double totalAvailableHours) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAvailableHours = totalAvailableHours;

        // When the block is first created, empty sya so remaining hours equals to the total available hours sa simula
        this.remainingHours = totalAvailableHours;
    }

    public TimeBlock(LocalDate date, double totalAvailableHours) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.totalAvailableHours = totalAvailableHours;
        this.remainingHours = totalAvailableHours;

        // Give it default times just so it doesn't crash if something asks for it later
        this.startTime = LocalTime.of(8, 0); // 8:00 AM
        this.endTime = LocalTime.of(22, 0);  // 10:00 PM
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDayOfWeekName() {
    return this.date.getDayOfWeek().toString(); 
}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Task> getAssignedTasks() { return assignedTasks; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public double getTotalAvailableHours() { return totalAvailableHours; }
    public void setTotalAvailableHours(double totalAvailableHours) { this.totalAvailableHours = totalAvailableHours; }

    public double getRemainingHours() { return remainingHours; }
    public void setRemainingHours(double remainingHours) { this.remainingHours = remainingHours; }

}
