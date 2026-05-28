package com.buzybee.app.service;

import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.exception.NoAvailabilityException;
import com.buzybee.app.scheduler.FractionalKnapsack;
import com.buzybee.app.scheduler.GreedyTaskScheduler;

import java.util.List;


/**
 * Orchestrates the scheduling pipeline.
 * Coordinates between the Knapsack algorithm (prioritization) and the
 * Greedy Scheduler (allocation) to generate the final weekly schedule.
 */
public class SchedulerService {

    // The Service owns the two math engines
    private FractionalKnapsack knapsackAlgorithm;
    private GreedyTaskScheduler greedyScheduler;

    public SchedulerService() {
        this.knapsackAlgorithm = new FractionalKnapsack();
        this.greedyScheduler = new GreedyTaskScheduler();
    }

    // ==========================================
    // Pipeline
    // ==========================================

    public void runPrioritizationOnly(List<Task> activeTasks) {
        System.out.println("=== RUNNING PRIORITIZATION ONLY ===");
        knapsackAlgorithm.prioritizeTasks(activeTasks);
        System.out.println("=== PRIORITIZATION COMPLETE ===");
    }

    public void runSchedulingPipeline(List<Task> activeTasks, List<TimeBlock> availableSchedule, String mode) {
        System.out.println("=== STARTING SCHEDULING PIPELINE ===");

        // Step 1: Hand the list to Knapsack to be scored and sorted
        knapsackAlgorithm.prioritizeTasks(activeTasks);

        // Step 2: Hand the sorted list and the schedule to the Greedy Algorithm
        try {
            greedyScheduler.assignTasksToTimeBlocks(activeTasks, availableSchedule, mode);
        } catch (NoAvailabilityException e) {
            System.out.println("ALERT: " + e.getMessage());
        }

        System.out.println("=== PIPELINE COMPLETE ===");
    }
}