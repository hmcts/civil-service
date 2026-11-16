package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.HwFOutcomeNotifyParties;

@Component
public class NotifyHwFOutcomePartiesNotifier extends Notifier<CaseData> {

    private static final String TASK_ID = HwFOutcomeNotifyParties.toString();

    public NotifyHwFOutcomePartiesNotifier(NotificationService notificationService,
                                           CaseTaskTrackingService caseTaskTrackingService,
                                           NotifyHwFOutcomePartiesAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}

