package com.buzybee.app.scheduler;

import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.exception.NoAvailabilityException;
import java.util.List;

public class GreedyTaskScheduler {

    // The Service calls this after the Knapsack is done sorting
    public void assignTasksToTimeBlocks(List<Task> sortedTasks, List<TimeBlock> schedule) throws NoAvailabilityException {
        System.out.println("--- 2. RUNNING GREEDY SCHEDULER ---");

        // TODO: The Greedy loop goes here
        // 1. Pick the first task
        // 2. Find the first available TimeBlock
        // 3. Subtract task time from block's remainingHours
        // 4. If task is bigger than remaining time, split.

        System.out.println("Assigning tasks to schedule...");
    }
}