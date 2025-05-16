package uk.gov.hmcts.reform.civil.notification.handlers.trialready.applicantnotifyotherstrialready;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ApplicantNotifyOthersTrialReady;

@Component
public class ApplicantNotifyOthersTrialReadyNotifier extends Notifier {

    public ApplicantNotifyOthersTrialReadyNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                   ApplicantNotifyOthersTrialReadyPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ApplicantNotifyOthersTrialReady.toString();
    }
}
