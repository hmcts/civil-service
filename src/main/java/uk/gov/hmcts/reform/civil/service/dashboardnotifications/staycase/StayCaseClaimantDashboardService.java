package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staycase;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_STAYED_JR_CANCELLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import org.springframework.stereotype.Component;

@Component
public class StayCaseClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;

    public StayCaseClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                             DashboardNotificationsParamsMapper mapper,
                                             DashboardNotificationService dashboardNotificationService,
                                             TaskListService taskListService,
                                            FeatureToggleService toggleService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.featureToggleService = toggleService;
    }

    public void notifyStayCase(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (featureToggleService.isJudgmentBufferEnabled()
            && YesOrNo.YES.equals(caseData.getIsJoRequested())) {
            return SCENARIO_AAA6_CASE_STAYED_JR_CANCELLED_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT.getScenario();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        final String role = "CLAIMANT";
        final String GA = "Applications";
        CaseState preStayState = CaseState.valueOf(caseData.getPreStayState());
        if (preStayState != CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT
            && preStayState != CaseState.AWAITING_APPLICANT_INTENTION) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(
                caseId, role
            );
        }

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            caseId,
            role,
            GA
        );
    }
}
