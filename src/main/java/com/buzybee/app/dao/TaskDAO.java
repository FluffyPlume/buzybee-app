package com.buzybee.app.dao;

import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) that manages the application's task list in memory.
 * Acts as the primary interface between the Service layer and the Persistence layer.
 */
public class TaskDAO {

    // The Master List: Holds every task in the app's memory
    private List<Task> allTasks;
    private List<TimeBlock> allTimeBlocks;
    private JsonHelper jsonHelper;

    // ==========================================
    // 1. CONSTRUCTOR
    // ==========================================
    public TaskDAO() {
        this.jsonHelper = new JsonHelper();

        // The second the DAO is created, it loads the data from the hard drive!
        this.allTasks = jsonHelper.loadTasks();
        this.allTimeBlocks = jsonHelper.loadTimeBlocks();

        // Failsafe: If the file was empty, make sure the list isn't null
        if (this.allTasks == null) {
            this.allTasks = new ArrayList<>();
        }
    }

    // ==========================================
    // 2. BASIC OPERATIONS
    // ==========================================

    // Add a new task and immediately save it
    public void addTask(Task newTask) {
        allTasks.add(newTask);
        jsonHelper.saveTasks(allTasks);
    }

    // Delete a task by its UUID and save
    public void deleteTask(String taskId) {
        allTasks.removeIf(task -> task.getId().equals(taskId));
        jsonHelper.saveTasks(allTasks);
    }

    public void saveWeeklySchedule(List<TimeBlock> newWeek) {
        this.allTimeBlocks = newWeek;
        jsonHelper.saveTimeBlocks(this.allTimeBlocks); // Saves to hard drive
    }

    public List<TimeBlock> getAllTimeBlocks() {
        return this.allTimeBlocks;
    }

    // Force a save (useful when updating an existing task's status)
    public void saveChanges() {
        jsonHelper.saveTasks(allTasks);
    }

    // ==========================================
    // 3. Filter Views
    // ==========================================

    // Gets all tasks that still need to be done
    public List<Task> getActiveTasks() {
        List<Task> active = new ArrayList<>();
        for (Task t : allTasks) {
            if (t.getStatus().equals("PENDING") || t.getStatus().equals("SCHEDULED")) {
                active.add(t);
            }
        }
        return active;
    }

    // Gets all tasks that missed their deadline
    public List<Task> getBacklogTasks() {
        List<Task> backlog = new ArrayList<>();
        for (Task t : allTasks) {
            if (t.getStatus().equals("BACKLOG")) {
                backlog.add(t);
            }
        }
        return backlog;
    }

    // Gets literally everything
    public List<Task> getAllTasks() {
        return allTasks;
    }
}