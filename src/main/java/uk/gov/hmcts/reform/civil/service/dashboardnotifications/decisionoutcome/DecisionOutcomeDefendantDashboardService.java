package uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionoutcome;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Objects;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME;

@Service
public class DecisionOutcomeDefendantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public DecisionOutcomeDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationService dashboardNotificationService,
                                                   DashboardNotificationsParamsMapper mapper,
                                                   TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyDecisionOutcome(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return AllocatedTrack.SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || Objects.nonNull(caseData.getTrialReadyRespondent1())
            ? SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME.getScenario()
            : SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseId,
            DEFENDANT_ROLE
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            DEFENDANT_ROLE
        );
    }
}
