package com.buzybee.app.scheduler;

import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.buzybee.app.exception.NoAvailabilityException;
import java.util.List;

public class GreedyTaskScheduler {

    // The Service calls this after the Knapsack is done sorting
    public void assignTasksToTimeBlocks(List<Task> sortedTasks, List<TimeBlock> schedule, String mode) throws NoAvailabilityException {
        System.out.println("--- 2. RUNNING GREEDY SCHEDULER in " + mode + "MODE ---");

        if (mode.equals("FRONTLOAD")) {
            for (Task task : sortedTasks){ // Pick up each task one by one (starting with the highest priority)
                double hoursLeftToSchedule = task.getEstimatedTime(); // Tracks how many hours of this task still need to be place
                for (TimeBlock block : schedule){ // Walk through the 7 day schedule from Monday to Sunday
                    if(hoursLeftToSchedule <= 0){ // If finished scheduling the task, stop looking for blocks!
                        break;
                    }
                    if (block.getRemainingHours() > 0){ // If timeblock has free time, do this
                        double hoursToAssign = Math.min(hoursLeftToSchedule, block.getRemainingHours()); // Basically checks which one is smaller para ayun ung hours na ilalagay, so no cases na 2hrs lng ung monday pero 4 hrs ung task so pipiliin is ung 2 since un lang kasya
                        block.getAssignedTasks().add(task); // Add task to that day's assigned list
                        block.setRemainingHours(block.getRemainingHours() - hoursToAssign); // Deduct the hours from the day's capacity
                        hoursLeftToSchedule -= hoursToAssign; // Deduct the hours from the task's remaining work
                        task.getAssignedTimeSlots().add(block.getDayOfWeekName() + " (" + hoursToAssign + " hrs"); // Keep History record of task itself
                    }
                }
                if (hoursLeftToSchedule > 0){
                    System.out.println("WARNING: Not enough free time in the week to finish " + task.getName());
                    task.setStatus("FAILED_CONSTRAINT");
                }
                else { task.setStatus("SCHEDULED"); }
            }
        }
        else if (mode.equals("BACKLOAD")) {
            for (Task task : sortedTasks){
                double hoursLeftToSchedule = task.getEstimatedTime();

                if ("BACKLOG".equals(task.getStatus())){
                    for (TimeBlock block : schedule){ // LOOP FORWARD
                        if (hoursLeftToSchedule <= 0) break;
                        if (block.getRemainingHours() > 0){
                            double hoursToAssign = Math.min(hoursLeftToSchedule, block.getRemainingHours());
                            block.getAssignedTasks().add(task);
                            block.setRemainingHours(block.getRemainingHours() - hoursToAssign);
                            hoursLeftToSchedule -= hoursToAssign;
                            task.getAssignedTimeSlots().add(block.getDayOfWeekName() + " (" + hoursToAssign + " hrs");
                        }
                    }
                }

                else {
                    for (int i = schedule.size() - 1; i >= 0; i--) { // Walk backwards from the end of the schedule to the beginning
                        TimeBlock block = schedule.get(i);
                        if (hoursLeftToSchedule <= 0) {
                            break;
                        }

                        if (block.getDate().isAfter(task.getDeadline())) { // Skip this day if it is AFTER the task's deadline
                            continue;
                        }

                        if (block.getRemainingHours() > 0) {
                            double hoursToAssign = Math.min(hoursLeftToSchedule, block.getRemainingHours());

                            block.getAssignedTasks().add(task);
                            block.setRemainingHours(block.getRemainingHours() - hoursToAssign);
                            hoursLeftToSchedule -= hoursToAssign;
                            task.getAssignedTimeSlots().add(block.getDayOfWeekName() + " (" + hoursToAssign + " hrs");
                        }
                    }
                }

                if (hoursLeftToSchedule > 0){
                    System.out.println("IMPOSSIBLE SCHEDULE DETECTED for:  " + task.getName());

                    // Force the remaining hours into the absolulte earliest slots available
                    for (TimeBlock block : schedule){
                        if (hoursLeftToSchedule <= 0 ){ break; }
                        double hoursToAssign = Math.min(hoursLeftToSchedule, block.getRemainingHours());
                        block.setRemainingHours(block.getRemainingHours() - hoursToAssign);
                        hoursLeftToSchedule -= hoursToAssign;

                        // Mark as emergency allocation
                        task.getAssignedTimeSlots().add(block.getDayOfWeekName() + " (" + hoursToAssign +  " hrs) [OVERALLOCATED]");

                    }
                }
                if (hoursLeftToSchedule > 0){ task.setStatus("FAILED_CONSTRAINT"); } // Impossible: Even using the entire week, not enough hours
                else if (task.getAssignedTimeSlots().stream().anyMatch(slot -> slot.contains("OVERALLOCATED"))) { task.setStatus("OVERALLOCATED_WARNING"); } // It fits but only by breaking the backload rule
                else { task.setStatus("SCHEDULED"); }
            }
        }

        System.out.println("Assigning tasks to schedule...");
    }
}