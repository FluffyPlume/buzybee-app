package com.buzybee.app.service;

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

    private TaskDAO taskDAO;
    private SchedulerService schedulerService;

    public TaskService(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
        this.schedulerService = new SchedulerService();
    }

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

    // Pass-through methods for the UI to get data
    public List<Task> getAllActiveTasks() {
        return taskDAO.getActiveTasks();
    }

    public List<Task> getBacklogTasks() {
        return taskDAO.getBacklogTasks();
    }

    // ==========================================
    // 2. THE ALGORITHM TRIGGER
    // ==========================================


    public void generateWeeklySchedule() {
        List<Task> tasksToSchedule = taskDAO.getActiveTasks();
        List<TimeBlock> savedSchedule = taskDAO.getAllTimeBlocks();

        schedulerService.runSchedulingPipeline(tasksToSchedule, savedSchedule);

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
}