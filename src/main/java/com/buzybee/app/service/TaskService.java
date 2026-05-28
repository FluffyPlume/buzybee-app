package com.buzybee.app.service;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.exception.InvalidTaskException;
import com.buzybee.app.exception.TaskOverdueException;
import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.util.SystemClock;
import java.util.List;

/**
 * The 'General Manager' of the application.
 * Handles validation, orchestrates communication between the DAO and the
 * Scheduler, and manages the primary business logic flow.
 */
public class TaskService {

    private TaskDAO taskDAO = SessionManager.getInstance().getTaskDao();
    private SchedulerService schedulerService = new SchedulerService();

    public TaskService() {}

    // ==========================================
    // 1. VALIDATION & CRUD (Create, Read, Update, Delete)
    // ==========================================

    public void createNewTask(Task newTask) throws InvalidTaskException, TaskOverdueException {
        // Validation Checks
        if (newTask.getName() == null || newTask.getName().trim().isEmpty()) {
            throw new InvalidTaskException("Task name cannot be empty!");
        }
        if (newTask.getEstimatedTime() <= 0) {
            throw new InvalidTaskException("Task must take greater than 0 hours.");
        }
        if (newTask.getDeadline() == null) {
            throw new InvalidTaskException("You must select a deadline.");
        }
        if (newTask.getDeadline().isBefore(SystemClock.today())) {
            throw new TaskOverdueException("You cannot schedule a task for a date in the past!");
        }

        // Save to Database
        taskDAO.addTask(newTask);
    }

    public void deleteTask(String taskId) {
        taskDAO.deleteTask(taskId);
    }

    // ==========================================
    // UI DATA FETCHERS (Updated for Pending/Scheduled Split)
    // ==========================================

    public List<Task> getPendingTasks() {
        return taskDAO.getPendingTasks();
    }

    public List<Task> getScheduledTasks() {
        return taskDAO.getScheduledTasks();
    }

    public List<Task> getBacklogTasks() {
        return taskDAO.getBacklogTasks();
    }

    // Helper method for the algorithm: combines Pending and Scheduled tasks
    public List<Task> getAllActiveTasks() {
        List<Task> allActive = new java.util.ArrayList<>();
        allActive.addAll(taskDAO.getPendingTasks());
        allActive.addAll(taskDAO.getScheduledTasks());
        return allActive;
    }

    // ==========================================
    // 2. THE ALGORITHM TRIGGER
    // ==========================================

    public void prioritizeActiveTasks() {
        List<Task> tasksToScore = new java.util.ArrayList<>();
        tasksToScore.addAll(getAllActiveTasks()); // Uses the helper method!
        tasksToScore.addAll(taskDAO.getBacklogTasks());

        schedulerService.runPrioritizationOnly(tasksToScore);

        // Save the new Priority Scores to the JSON file
        taskDAO.saveChanges();
    }

    public void generateWeeklySchedule(String mode) {
        List<Task> tasksToSchedule = new java.util.ArrayList<>();
        tasksToSchedule.addAll(getAllActiveTasks()); // Uses the helper method!
        tasksToSchedule.addAll(taskDAO.getBacklogTasks());
        List<TimeBlock> savedSchedule = taskDAO.getAllTimeBlocks();

        schedulerService.runSchedulingPipeline(tasksToSchedule, savedSchedule, mode);

        taskDAO.saveChanges();
    }

    public void createWeeklySchedule(List<TimeBlock> completeWeek) throws InvalidTaskException {
        if (completeWeek == null || completeWeek.size() != 7) {
            throw new InvalidTaskException("A weekly schedule must contain exactly 7 days.");
        }

        // Ultimate Backend Validation: Check if the start date is in the past
        if (completeWeek.get(0).getDate().isBefore(SystemClock.today())) {
            throw new InvalidTaskException("You cannot start a schedule in the past.");
        }

        // Overwrite the old schedule with the new one
        taskDAO.saveWeeklySchedule(completeWeek);
    }

    // ==========================================
    // 3. TASK COMPLETION
    // ==========================================

    // Marks a task as complete and records the exact date it was finished
    public void markTaskComplete(String taskId) {
        for (Task t : taskDAO.getAllTasks()) {
            if (t.getId().equals(taskId)) {
                t.setStatus("COMPLETED");
                t.setDateCompleted(SystemClock.today());
                taskDAO.saveChanges();
                break;
            }
        }
    }

    // Reverses a completed task back to PENDING and clears the completion date
    public void unmarkTaskComplete(String taskId) {
        for (Task t : taskDAO.getAllTasks()) {
            if (t.getId().equals(taskId)) {
                t.setStatus("PENDING");
                t.setDateCompleted(null); // Erase the finish date!
                taskDAO.saveChanges();
                break;
            }
        }
    }
}