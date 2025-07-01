package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed.ClaimantConfirmProceedDefendantEmailDTOGenerator.NO_EMAIL_OPERATION;

@AllArgsConstructor
@Slf4j
public abstract class BaseNotifier {

    protected final NotificationService notificationService;

    protected List<String> sendNotification(Set<EmailDTO> recipients) {
        List<String> errorMessages = new ArrayList<>();
        for (EmailDTO recipient : recipients) {
            if (NO_EMAIL_OPERATION.equals(recipient.getEmailTemplate())) {
                log.info("Skipping notification for id {} due to no op request", recipient.getReference());
                continue;
            }
            try {
                notificationService.sendMail(
                    recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                    recipient.getReference()
                );
            } catch (NotificationException e) {
                errorMessages.add(e.getMessage());
            }
        }
        return errorMessages;
    }
}
