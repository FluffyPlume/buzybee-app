package com.buzybee.app.scheduler;

import com.buzybee.app.model.Task;
import java.util.List;

public class FractionalKnapsack {

    // This is the only public method. The Service calls this.
    public void prioritizeTasks(List<Task> activeTasks) {
        System.out.println("--- 1. RUNNING KNAPSACK ALGORITHM ---");
        calculatePriorityScores(activeTasks);
        sortTasksByPriority(activeTasks);
    }

    private void calculatePriorityScores(List<Task> tasks) {
        System.out.println("Calculating Knapsack Priority Scores...");
        for (Task task : tasks) {
            // 1. Calculate days remaining until the deadline
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), task.getDeadline());

            if (daysRemaining < 0 && task.getStatus().equals("PENDING")){
                task.setStatus("BACKLOG");
            }

            // Failsafe: If a task is due today or overdue, treat it as 1 day to prevent dividing by zero
            if (daysRemaining <= 0) {
                daysRemaining = 1;
            }

            // 2. Define Urgency (Closer deadline = higher urgency number)
            double urgency = 10.0 / daysRemaining;

            // 3. The Core Knapsack Formula
            double score = (task.getDifficulty() * urgency) / Math.sqrt(task.getEstimatedTime());

            // 4. The Rules: Overdue tasks get a 2x priority boost
            if ("BACKLOG".equals(task.getStatus())) {
                score *= 2.0;
            }

            // Save the final mathematical score to the task
            task.setPriorityScore(score);
        }
    }

    private void sortTasksByPriority(List<Task> tasks) {
        System.out.println("Sorting tasks from highest to lowest priority...");
        // This sorts the list in descending order based on the priority score we just calculated
        tasks.sort((t1, t2) -> Double.compare(t2.getPriorityScore(), t1.getPriorityScore()));
    }
}