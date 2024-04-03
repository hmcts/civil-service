package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED;

@Service
public class GenerateDashboardNotificationClaimFeeRequiredHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CLAIMANT1);
    public static final String TASK_ID = "GenerateDashboardNotificationClaimFeeRequired";

    public GenerateDashboardNotificationClaimFeeRequiredHandler(DashboardApiClient dashboardApiClient,
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
        return SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED.getScenario();
    }
}
