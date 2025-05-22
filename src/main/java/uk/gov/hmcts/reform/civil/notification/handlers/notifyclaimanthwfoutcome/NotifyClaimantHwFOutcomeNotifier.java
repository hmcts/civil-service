package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.HwFOutcomeNotify;

@Component
public class NotifyClaimantHwFOutcomeNotifier extends Notifier {

    private static final String TASK_ID = HwFOutcomeNotify.toString();

    public NotifyClaimantHwFOutcomeNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator allLegalRepsEmailGenerator) {
            super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}

