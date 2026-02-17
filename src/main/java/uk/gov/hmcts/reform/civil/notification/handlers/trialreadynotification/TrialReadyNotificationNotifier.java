package uk.gov.hmcts.reform.civil.notification.handlers.trialreadynotification;

import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationNotifier extends Notifier {

    public TrialReadyNotificationNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                          TrialReadyNotificationAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);

    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.TrialReadyNotificationNotifier.toString();
    }
}
