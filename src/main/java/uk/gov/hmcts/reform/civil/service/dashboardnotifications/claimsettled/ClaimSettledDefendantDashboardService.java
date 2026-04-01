package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimsettled;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

@Service
public class ClaimSettledDefendantDashboardService extends DashboardScenarioService {


    protected static final String DEFENDANT = "DEFENDANT";
    protected final FeatureToggleService featureToggleService;
    protected final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public ClaimSettledDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper, FeatureToggleService featureToggleService,
                                                 DashboardNotificationService dashboardNotificationService, TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyClaimSettled(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());

        if (!featureToggleService.isLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())) {
            inactiveGAItems(caseId);
        }

    }

    private void inactiveGAItems(String caseId) {
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseId,
            DEFENDANT
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
            caseId,
            DEFENDANT,
            "Application.View"
        );
    }
}
