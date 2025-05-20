package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public abstract class Notifier {

    protected final NotificationService notificationService;
    protected final CaseTaskTrackingService caseTaskTrackingService;
    protected final PartiesEmailGenerator partiesNotifier;

    public void notifyParties(CaseData caseData, String eventId, String taskId) {
        log.info("Notifying parties for case ID: {} and eventId: {} and taskId: {} ", caseData.getCcdCaseReference(), eventId, taskId);
        final Set<EmailDTO> partiesToEmail = partiesNotifier.getPartiesToNotify(caseData, taskId);
        final List<String> errors = sendNotification(partiesToEmail);
        if (!errors.isEmpty()) {
            final HashMap<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put("Errors", errors.toString());
            trackErrors(caseData.getCcdCaseReference(), eventId, taskId, additionalProperties);
        }
    }

    private List<String> sendNotification(Set<EmailDTO> recipients) {
        List<String> errorMessages = new ArrayList<>();
        for (EmailDTO recipient : recipients) {
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

    private void trackErrors(final Long caseId,
                             final String eventId,
                             final String taskId,
                             final Map<String, String> additionalProperties) {
        caseTaskTrackingService.trackCaseTask(
            caseId.toString(),
            eventId,
            taskId,
            additionalProperties
        );
    }

    protected abstract String getTaskId();
}
