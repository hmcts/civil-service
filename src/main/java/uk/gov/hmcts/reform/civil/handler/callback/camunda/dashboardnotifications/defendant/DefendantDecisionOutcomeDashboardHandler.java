package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME;

@Service
public class DefendantDecisionOutcomeDashboardHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME);
    public static final String TASK_ID = "GenerateDashboardDefendantDecisionOutcome";
    private static final String DEFENDANT_ROLE = "DEFENDANT";

    public DefendantDecisionOutcomeDashboardHandler(DashboardApiClient dashboardApiClient,
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
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            DEFENDANT_ROLE,
            authToken
        );

        dashboardApiClient.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            DEFENDANT_ROLE,
            authToken
        );
    }

    @Override
    public String getScenario(CaseData caseData) {
        return AllocatedTrack.SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || Objects.nonNull(caseData.getTrialReadyRespondent1())
            ? SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME.getScenario()
            : SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME.getScenario();
    }
}
