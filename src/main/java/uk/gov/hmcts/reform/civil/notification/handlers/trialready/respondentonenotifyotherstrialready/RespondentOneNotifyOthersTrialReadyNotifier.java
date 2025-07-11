package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondentonenotifyotherstrialready;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.Respondent1NotifyOthersTrialReady;

@Component
public class RespondentOneNotifyOthersTrialReadyNotifier extends Notifier {

    public RespondentOneNotifyOthersTrialReadyNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                       RespondentOneNotifyOthersTrialReadyPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return Respondent1NotifyOthersTrialReady.toString();
    }
}
