package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationClient notificationClient;

    public void sendMail(
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        try {
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    public void sendNotifications(
        List<String> targetEmails,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future> tasks = new ArrayList<>();
        log.info("About to send " + targetEmails.size() + " notifications.");
        targetEmails.forEach(email -> {
            log.info("Preparing to send notification: " + email);
            Future task = executorService.submit(() -> sendMail(email, emailTemplate, parameters, reference));
            tasks.add(task);
        });
        monitorNotificationTasks(tasks);
        executorService.shutdown();
        waitForNotificationTasksToFinish(executorService);
    }

    private void monitorNotificationTasks(List<Future> tasks) {
        try {
            for (Future task : tasks) {
                task.get();
            }
        } catch (ExecutionException e) {
            throw new NotificationException("There was a problem sending the notification.");
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void waitForNotificationTasksToFinish(ExecutorService executorService) {
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}


