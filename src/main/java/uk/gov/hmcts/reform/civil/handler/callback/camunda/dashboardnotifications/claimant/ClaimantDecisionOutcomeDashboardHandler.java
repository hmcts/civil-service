package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_TASK_LIST_CLAIMANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_TRIAL_READY_DECISION_OUTCOME;

@Service
public class ClaimantDecisionOutcomeDashboardHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_DASHBOARD_TASK_LIST_CLAIMANT_DECISION_OUTCOME);
    public static final String TASK_ID = "GenerateDashboardClaimantDecisionOutcome";
    private static final String CLAIMANT_ROLE = "CLAIMANT";
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public ClaimantDecisionOutcomeDashboardHandler(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationsParamsMapper mapper,
                                                   FeatureToggleService featureToggleService,
                                                   DashboardNotificationService dashboardNotificationService,
                                                   TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseId,
            CLAIMANT_ROLE
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            CLAIMANT_ROLE,
            null
        );
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
        return AllocatedTrack.SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || Objects.nonNull(caseData.getTrialReadyApplicant())
            ? SCENARIO_AAA6_CLAIMANT_TRIAL_READY_DECISION_OUTCOME.getScenario()
            : SCENARIO_AAA6_CLAIMANT_DECISION_OUTCOME.getScenario();
    }

}
