package uk.gov.hmcts.reform.civil.notify;

import org.jspecify.annotations.NonNull;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            log.info("NotificationService::sendMail::templateID: {}", emailTemplate);
            SendEmailResponse sendEmailResponse = notificationClient.sendEmail(
                emailTemplate,
                targetEmail,
                parameters,
                reference
            );
            log.info("NotificationService::sendMail::successful for reference: {}, notificationID: {}",
                     reference,
                     getNotificationId(sendEmailResponse)
            );
        } catch (NotificationClientException e) {
            log.error("NotificationService::sendMail::error for reference: {}, message: {}", reference, e.getMessage());
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

    private @NonNull String getNotificationId(SendEmailResponse sendEmailResponse) {
        return Optional.ofNullable(sendEmailResponse)
            .map(SendEmailResponse::getNotificationId)
            .map(Object::toString)
            .orElse("NOTSET");
    }
}
