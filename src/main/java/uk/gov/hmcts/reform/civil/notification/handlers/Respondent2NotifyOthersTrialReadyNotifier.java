package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.Respondent2NotifyOthersTrialReady;

@Component
public class Respondent2NotifyOthersTrialReadyNotifier extends TrialReadyNotifier {

    public Respondent2NotifyOthersTrialReadyNotifier(NotificationService notificationService,
                                                     NotificationsProperties notificationsProperties,
                                                     OrganisationService organisationService,
                                                     SimpleStateFlowEngine stateFlowEngine,
                                                     CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    protected String getTaskId() {
        return Respondent2NotifyOthersTrialReady.toString();
    }
}
