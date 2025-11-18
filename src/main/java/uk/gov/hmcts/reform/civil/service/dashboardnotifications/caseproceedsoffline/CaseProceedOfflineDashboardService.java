package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;

public abstract class CaseProceedOfflineDashboardService extends DashboardScenarioService {

    private static final List<CaseState> CASE_PROCEEDS_IN_CASEMAN_STATES = List.of(
        CaseState.AWAITING_APPLICANT_INTENTION,
        CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
        CaseState.IN_MEDIATION,
        CaseState.JUDICIAL_REFERRAL
    );
    private static final List<CaseState> CASE_PROGRESSION_STATES = List.of(
        CaseState.CASE_PROGRESSION,
        CaseState.HEARING_READINESS,
        CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING,
        CaseState.DECISION_OUTCOME,
        CaseState.All_FINAL_ORDERS_ISSUED
    );
    protected static final String GA_CATEGORY = "Applications";

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    protected CaseProceedOfflineDashboardService(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper,
                                                 DashboardNotificationService dashboardNotificationService,
                                                 TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyCaseProceedOffline(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        boolean previousStatePresent = caseData.getPreviousCCDState() != null;
        boolean eligibleCasemanState = previousStatePresent
            && CASE_PROCEEDS_IN_CASEMAN_STATES.contains(caseData.getPreviousCCDState())
            && eligibleForCasemanState(caseData);

        return eligibleCasemanState || shouldRecordScenarioInCaseProgression(caseData);
    }

    protected boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        boolean previousStatePresent = caseData.getPreviousCCDState() != null;
        return previousStatePresent
            && CASE_PROGRESSION_STATES.contains(caseData.getPreviousCCDState())
            && eligibleForCaseProgressionState(caseData);
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, citizenRole());
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            caseId,
            citizenRole(),
            GA_CATEGORY
        );
    }

    protected abstract String citizenRole();

    protected abstract boolean eligibleForCasemanState(CaseData caseData);

    protected abstract boolean eligibleForCaseProgressionState(CaseData caseData);
}
