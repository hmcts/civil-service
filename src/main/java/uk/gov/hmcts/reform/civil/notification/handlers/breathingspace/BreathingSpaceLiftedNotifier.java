package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class BreathingSpaceLiftedNotifier extends Notifier {

    public BreathingSpaceLiftedNotifier(NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService, BreathingSpaceLiftedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.BreathingSpaceLiftedNotifyParties.toString();
    }
}
