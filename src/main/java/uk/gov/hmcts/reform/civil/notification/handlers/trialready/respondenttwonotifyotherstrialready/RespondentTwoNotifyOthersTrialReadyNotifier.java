package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondenttwonotifyotherstrialready;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.Respondent2NotifyOthersTrialReady;

@Component
public class RespondentTwoNotifyOthersTrialReadyNotifier extends Notifier {


    public RespondentTwoNotifyOthersTrialReadyNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                       RespondentTwoNotifyOthersTrialReadyPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return Respondent2NotifyOthersTrialReady.toString();
    }
}
