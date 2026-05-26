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
        // TODO: (Difficulty * Urgency) / Estimated Time
    }

    private void sortTasksByPriority(List<Task> tasks) {
        System.out.println("Sorting tasks from highest to lowest priority...");
        // TODO: Collections.sort() goes here
    }
}