package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.Respondent1NotifyOthersTrialReady;

@Component
public class Respondent1NotifyOthersTrialReadyNotifier extends TrialReadyNotifier {

    public Respondent1NotifyOthersTrialReadyNotifier(NotificationService notificationService,
                                                     NotificationsProperties notificationsProperties,
                                                     OrganisationService organisationService,
                                                     SimpleStateFlowEngine stateFlowEngine,
                                                     CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    protected String getTaskId() {
        return Respondent1NotifyOthersTrialReady.toString();
    }
}
