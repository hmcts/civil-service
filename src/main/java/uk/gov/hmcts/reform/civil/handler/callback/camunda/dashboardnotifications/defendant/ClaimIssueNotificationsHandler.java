package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT;

@Service
@Slf4j
public class ClaimIssueNotificationsHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1);
    public static final String TASK_ID = "CreateIssueClaimDashboardNotificationsForDefendant1";

    public ClaimIssueNotificationsHandler(DashboardScenariosService dashboardScenariosService,
                                          DashboardNotificationsParamsMapper mapper,
                                          FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
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
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        AllocatedTrack allocatedTrack = AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null);
        List<DashboardScenarios> scenarioConditions = getScenarioConditions(caseData, allocatedTrack);

        scenarioConditions.forEach(scenario -> recordScenario(authToken, scenario, caseData));

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public String getScenario(CaseData caseData) {
        return null;
    }

    private List<DashboardScenarios> getScenarioConditions(CaseData caseData, AllocatedTrack allocatedTrack) {
        boolean isUnrepresented = caseData.isRespondent1NotRepresented();
        List<DashboardScenarios> scenarios = new ArrayList<>();
        if (isUnrepresented) {
            scenarios.add(SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED);

            if (FAST_CLAIM.equals(allocatedTrack)) {
                scenarios.add(SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT);
            }
        }

        return scenarios;
    }

    private void recordScenario(String authToken, DashboardScenarios dashboardScenarios, CaseData caseData) {
        dashboardScenariosService.recordScenarios(
            authToken,
            dashboardScenarios.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData)).build()
        );
    }

}
