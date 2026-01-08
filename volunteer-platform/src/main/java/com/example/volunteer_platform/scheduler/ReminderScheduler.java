package com.example.volunteer_platform.scheduler;

import com.example.volunteer_platform.service.EmailService;
import com.example.volunteer_platform.repository.TaskSignupRepository;
import com.example.volunteer_platform.model.TaskSignup;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler responsible for sending reminder emails
 * to volunteers about their upcoming tasks.
 *
 * This job is intended to run periodically (e.g., daily)
 * and ensures reminders are sent only once per task signup.
 */
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    // Logger for monitoring scheduler execution
    private static final Logger logger =
            LoggerFactory.getLogger(ReminderScheduler.class);

    // Repository to fetch task signup data
    private final TaskSignupRepository taskSignupRepository;

    // Service used to send reminder emails
    private final EmailService emailService;

    /**
     * Sends reminder emails for volunteer tasks scheduled for tomorrow.
     *
     * - Fetches upcoming tasks with reminderSent = false
     * - Sends personalized reminder emails
     * - Marks reminders as sent to prevent duplicates
     */
    @Transactional
    // @Scheduled(cron = "0 0 8 * * *") // Uncomment to run every day at 8 AM
    public void sendTaskReminders() {

        // Calculate tomorrow's date
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Fetch all upcoming task signups that have not received reminders
        List<TaskSignup> upcomingTasks =
                taskSignupRepository.findUpcomingTaskSignups(
                        tomorrow,
                        false // reminderSent = false
                );

        // Log how many reminders need to be processed
        logger.info("Found {} upcoming task signups", upcomingTasks.size());

        // Process each task signup individually
        for (TaskSignup signup : upcomingTasks) {

            logger.info("Processing TaskSignup ID: {}", signup.getSignupId());

            // Build a friendly and informative email body
            String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Thank you for signing up as a volunteer with us! This is a friendly reminder about your upcoming task:\n\n" +
                    "Task Title: %s\n" +
                    "Date: %s\n" +
                    "Location: %s\n" +
                    "Description: %s\n\n" +
                    "We appreciate your dedication and commitment to making a difference.\n\n" +
                    "If you have any questions or need further assistance, feel free to contact us.\n\n" +
                    "Thank you once again for your time and efforts.\n\n" +
                    "Warm regards,\n" +
                    "The Volunteer Platform Team",
                    signup.getVolunteer().getName(),      // Volunteer name
                    signup.getTask().getTitle(),          // Task title
                    signup.getTask().getEventDate(),      // Event date
                    signup.getTask().getLocation(),       // Location
                    signup.getTask().getDescription()     // Task description
            );

            try {
                // Send reminder email to the volunteer
                emailService.sendReminderEmail(
                        signup.getVolunteer().getEmail(),
                        "Reminder: Upcoming Volunteer Task - " +
                                signup.getTask().getTitle(),
                        emailBody
                );

                // Mark reminder as sent to avoid duplicate emails
                signup.setReminderSent(true);
                taskSignupRepository.save(signup);

                // Log successful reminder delivery
                logger.info("Reminder sent for TaskSignup ID: {}",
                        signup.getSignupId());

                // Force immediate database synchronization
                taskSignupRepository.flush();

            } catch (Exception e) {
                // Log failures without interrupting the scheduler
                logger.error(
                        "Failed to send email to: {}",
                        signup.getVolunteer().getEmail(),
                        e
                );
            }
        }
    }
}
