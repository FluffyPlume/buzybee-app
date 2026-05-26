package com.buzybee.app;

import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.scheduler.FractionalKnapsack;
import com.buzybee.app.scheduler.GreedyTaskScheduler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("--- SETTING UP TEST DATA ---");

        // 1. Create Fake Tasks (Total: 7 hours of work)
        List<Task> dummyTasks = new ArrayList<>();
        dummyTasks.add(new Task("Math Homework", 8.0, 2.0, LocalDate.now().plusDays(2)));
        dummyTasks.add(new Task("Science Essay", 5.0, 4.0, LocalDate.now().plusDays(5)));

        // Force one to be a BACKLOG task to test your multiplier
        Task overdueTask = new Task("History Reading", 3.0, 1.0, LocalDate.now().minusDays(1));
        overdueTask.setStatus("BACKLOG");
        dummyTasks.add(overdueTask);

        // 2. Create Fake 7-Day Schedule (2 hours available each day)
        List<TimeBlock> dummySchedule = new ArrayList<>();
        LocalDate startDay = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            dummySchedule.add(new TimeBlock(
                    startDay.plusDays(i),
                    LocalTime.of(16, 0),  // Start at 4:00 PM
                    LocalTime.of(18, 0),  // End at 6:00 PM
                    2.0                   // 2 hours total capacity per day
            ));
        }

        // 3. Run the Knapsack
        FractionalKnapsack knapsack = new FractionalKnapsack();
        knapsack.prioritizeTasks(dummyTasks);

        // Print sorted tasks to verify the math worked
        System.out.println("\n--- KNAPSACK RESULTS ---");
        for (Task t : dummyTasks) {
            System.out.println(t.getName() + " | Score: " + t.getPriorityScore());
        }

        // 4. Run the Greedy Scheduler
        System.out.println("\n--- GREEDY SCHEDULER RESULTS ---");
        GreedyTaskScheduler greedy = new GreedyTaskScheduler();

        try {
            // Change "FRONTLOAD" to "BACKLOAD" later to test the reverse loop
            greedy.assignTasksToTimeBlocks(dummyTasks, dummySchedule, "FRONTLOAD");

            // 5. Print the final calendar to see where things landed
            for (TimeBlock block : dummySchedule) {
                System.out.println(block.getDayOfWeekName() + " (" + block.getDate() + ") - Remaining Hours: " + block.getRemainingHours());
                for (Task t : block.getAssignedTasks()) {
                    System.out.println("  -> Scheduled: " + t.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
