package com.example.volunteer_platform.scheduler;

import com.example.volunteer_platform.model.Task;
import com.example.volunteer_platform.repository.TaskRepository;
import com.example.volunteer_platform.enums.TaskStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler responsible for automatically updating task statuses
 * based on application deadlines and event dates.
 *
 * This helps ensure tasks move through their lifecycle
 * without requiring manual intervention.
 */
@Component
public class TaskStatusScheduler {

    // Repository used to fetch and update task records
    @Autowired
    private TaskRepository taskRepository;

    /**
     * Runs daily at midnight to update task statuses.
     *
     * Status transitions handled:
     * - AVAILABLE → APPLICATION_ENDED (if application deadline has passed)
     * - Any status → ENDED (if event date has passed)
     */
    @Scheduled(cron = "0 0 0 * * *") // Executes every day at 12:00 AM
    public void updateTaskStatuses() {

        // Fetch all tasks from the database
        List<Task> tasks = taskRepository.findAll();

        // Get today's date for comparison
        LocalDate today = LocalDate.now();

        // Iterate through each task to evaluate status updates
        for (Task task : tasks) {

            /*
             * If the application deadline has passed
             * and the task is still open for applications,
             * mark it as APPLICATION_ENDED
             */
            if (task.getApplicationDeadline().isBefore(today)
                    && task.getStatus() == TaskStatus.AVAILABLE) {

                task.setStatus(TaskStatus.APPLICATION_ENDED);
                taskRepository.save(task);
            }

            /*
             * If the event date has already passed
             * and the task is not yet marked as ENDED,
             * update the status accordingly
             */
            if (task.getEventDate().isBefore(today)
                    && task.getStatus() != TaskStatus.ENDED) {

                task.setStatus(TaskStatus.ENDED);
                taskRepository.save(task);
            }
        }
    }
}
