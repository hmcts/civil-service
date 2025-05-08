package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondenttwonotifyotherstrialready;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyNotifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

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
