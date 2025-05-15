package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class Notifier extends BaseNotifier {

    protected final CaseTaskTrackingService caseTaskTrackingService;
    protected final PartiesEmailGenerator partiesNotifier;

    protected Notifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                       PartiesEmailGenerator partiesNotifier) {
        super(notificationService);
        this.caseTaskTrackingService = caseTaskTrackingService;
        this.partiesNotifier = partiesNotifier;
    }

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
