package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

@Component
public class ApplicantNotifyOthersTrialReadyNotifier extends TrailReadyNotifier {

    private final static String TASK_ID = "ApplicantNotifyOthersTrialReadyNotifier";

    public ApplicantNotifyOthersTrialReadyNotifier(NotificationService notificationService,
                                                   NotificationsProperties notificationsProperties,
                                                   OrganisationService organisationService,
                                                   SimpleStateFlowEngine stateFlowEngine,
                                                   CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    protected String getTaskId() {
        return TASK_ID;
    }
}
