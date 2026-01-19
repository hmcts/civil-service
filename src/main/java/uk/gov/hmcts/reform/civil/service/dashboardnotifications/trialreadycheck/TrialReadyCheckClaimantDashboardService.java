package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT;

@Service
public class TrialReadyCheckClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public TrialReadyCheckClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationService dashboardNotificationService,
                                                   DashboardNotificationsParamsMapper mapper,
                                                   TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyTrialReadyCheck(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented())
            && isNull(caseData.getTrialReadyApplicant())
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack());
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
            CLAIMANT_ROLE
        );
    }
}
