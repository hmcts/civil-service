package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimdismissed;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

@Service
public class ClaimDismissedClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;

    public ClaimDismissedClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                  DashboardNotificationsParamsMapper mapper,
                                                  FeatureToggleService featureToggleService,
                                                  DashboardNotificationService dashboardNotificationService,
                                                  TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimDismissed(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_CCJ_CANCELLED_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return JudgmentsOnlineHelper.isJoRequested(caseData, featureToggleService);
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, CLAIMANT_ROLE);
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseId, CLAIMANT_ROLE);
    }
}
