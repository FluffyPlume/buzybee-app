package com.buzybee.app.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


/**
 * Represents an available scheduling window.
 * Tracks the total available hours and remaining capacity for
 * the Greedy Task Scheduling algorithm.
 */
public class TimeBlock {

    private String id;
    private String dayofWeek; // Monday
    private LocalDate date;
    private LocalTime startTime; // e.g., 4:00 PM
    private LocalTime endTime; // e.g., 8:00 PM

    private double totalAvailableHours; // e.g., 3.0
    private double remainingHours;

    // Constructor
    public TimeBlock(LocalDate date, String dayofWeek, LocalTime startTime, LocalTime endTime, double totalAvailableHours) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.dayofWeek = dayofWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAvailableHours = totalAvailableHours;

        // When the block is first created, empty sya so remaining hours equals to the total available hours sa simula
        this.remainingHours = totalAvailableHours;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDayofWeek() { return dayofWeek; }
    public void setDayofWeek(String dayofWeek) { this.dayofWeek = dayofWeek; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public double getTotalAvailableHours() { return totalAvailableHours; }
    public void setTotalAvailableHours(double totalAvailableHours) { this.totalAvailableHours = totalAvailableHours; }

    public double getRemainingHours() { return remainingHours; }
    public void setRemainingHours(double remainingHours) { this.remainingHours = remainingHours; }

}
