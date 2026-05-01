package com.buzybee.app.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Represents a single task entity with user-defined attributes.
 * This class acts as a data model, holding core identity, difficulty,
 * time estimates, and status information for persistence.
 */
public class Task {

    // Variables
    private String id; // Unique ID
    private String name; // Task name
    private double difficulty; // "1-10 rating", with 1 being the easiest and 10 with being difficult
    private double estimatedTime; // 2 hrs, 3 hrs, 4 hrs
    private LocalDate deadline; // deadline date duhh
    private LocalDate dateCompleted; // this is for history table purposes, future use gnun basta

    // Algorithm & System Variables
    private double priorityScore; // Ratio nung Knapsack Algo
    private String status; // "PENDING" , "SCHEDULED" , "BACKLOG" , "COMPLETED"
    private List<String> assignedTimeSlots;

    // Constructor
    public Task(String name, double difficulty, double estimatedTime, LocalDate deadline) {

        // Automatically generates Unique ID
        this.id = UUID.randomUUID().toString();

        this.name = name;
        this.difficulty = difficulty;
        this.estimatedTime = estimatedTime;
        this.deadline = deadline;

        //Default Settings for new entries
        this.status = "PENDING";
        this.priorityScore = 0;
        this.assignedTimeSlots = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getDifficulty() { return difficulty; }
    public void setDifficulty(double difficulty) { this.difficulty = difficulty; }

    public double getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(double priorityScore) { this.priorityScore = priorityScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getAssignedTimeSlots() { return assignedTimeSlots; }
    public void setAssignedTimeSlots(List<String> assignedTimeSlots) { this.assignedTimeSlots = assignedTimeSlots; }

    public LocalDate getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(LocalDate dateCompleted) { this.dateCompleted = dateCompleted; }

}
