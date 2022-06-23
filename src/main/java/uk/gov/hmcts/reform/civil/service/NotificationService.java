package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationClient notificationClient;

    public void sendMail(
        List<String> targetEmails,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        for (String email : targetEmails) {
            var failures = 0;
            try {
                System.out.println("About to send notification -");
                System.out.println("EMAIL: " + email);
                System.out.println("TEMPLATE: " + emailTemplate);
                sendMail(email, emailTemplate, parameters, reference);
            } catch (NotificationException e) {
                log.info("Failed to send notification: ", e);
                failures++;
            }

            if (failures == targetEmails.size()) {
                throw new NotificationException("Sending of all notifications failed");
            }
        }

    }

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
}
