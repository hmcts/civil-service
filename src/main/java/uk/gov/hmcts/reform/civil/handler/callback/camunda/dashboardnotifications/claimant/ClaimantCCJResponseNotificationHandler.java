package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@Service
public class ClaimantCCJResponseNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_CLAIMANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    public static final String TASK_ID = "GenerateClaimantCCJDashboardNotificationClaimantResponse";

    public ClaimantCCJResponseNotificationHandler(DashboardApiClient dashboardApiClient,
                                                  DashboardNotificationsParamsMapper mapper,
                                                  FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented());
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
        return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario();
    }
}
