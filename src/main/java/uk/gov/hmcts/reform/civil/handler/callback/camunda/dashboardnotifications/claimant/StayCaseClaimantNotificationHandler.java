package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT;

@Service
public class StayCaseClaimantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT);
    public static final String TASK_ID = "GenerateDashboardNotificationStayCaseClaimant";
    public static final String GA = "Applications";

    public StayCaseClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
                                               DashboardNotificationsParamsMapper mapper,
                                               FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT.getScenario();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {

        dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            authToken
        );
        dashboardApiClient.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            GA,
            authToken
        );
    }
}
