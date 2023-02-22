package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Service
@Slf4j
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
            log.info("Notification service sending email");
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
            log.info("Notification service email sent");
        } catch (NotificationClientException e) {
            log.info("Notification service notification exception" + e.getMessage());
            throw new NotificationException(e);
        }
    }

    public void sendLetter(String letterTemplate, Map<String, ?> personalisation, String reference) {
        try {
            notificationClient.sendLetter(letterTemplate, personalisation, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }
}
