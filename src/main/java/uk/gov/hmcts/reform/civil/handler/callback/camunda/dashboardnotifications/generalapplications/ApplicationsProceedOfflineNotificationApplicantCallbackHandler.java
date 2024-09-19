package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.generalapplications;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT;

public class ApplicationsProceedOfflineNotificationApplicantCallbackHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_APPLICANT);
    public static final String TASK_ID = "applicantLipApplicationOfflineDashboardNotification";
    private static final List<String> NON_LIVE_STATES = List.of(
        "Application Closed",
        "Proceeds In Heritage",
        "Order Made",
        "Application Dismissed"
    );

    public ApplicationsProceedOfflineNotificationApplicantCallbackHandler(DashboardApiClient dashboardApiClient,
                                                                          DashboardNotificationsParamsMapper mapper,
                                                                          FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected String getScenario(CaseData caseData) {
        List<Element<GeneralApplication>> generalApplications = caseData.getGeneralApplications();
        if (caseData.getGeneralApplications() != null
            && !caseData.getGeneralApplications().isEmpty()) {
            Element<GeneralApplication> liveApp = generalApplications.stream().filter(
                generalApp -> isLive(generalApp.getValue().getGeneralApplicationState())).findFirst().orElse(null);
            return liveApp != null ? SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT.getScenario() : "";
        }
        return "";
    }

    private boolean isLive(String applicationState) {
        return !NON_LIVE_STATES.contains(applicationState);
    }
}
