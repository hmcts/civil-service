package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@AllArgsConstructor
@Slf4j
public abstract class BaseNotifier {

    protected final NotificationService notificationService;

    protected List<String> sendNotification(Set<EmailDTO> recipients) {
        List<String> errorMessages = new ArrayList<>();
        for (EmailDTO recipient : recipients) {
            try {
                notificationService.sendMail(
                    recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                    recipient.getReference()
                );
            } catch (NotificationException e) {
                final String message = format("Failed to send email to %s : %s", recipient.getTargetEmail(), e.getMessage());
                errorMessages.add(message);
            }
        }
        return errorMessages;
    }
}
