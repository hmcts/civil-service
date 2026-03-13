package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT;

@Service
public class TrialArrangementsNotifyOtherPartyClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public TrialArrangementsNotifyOtherPartyClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                                     DashboardNotificationService dashboardNotificationService,
                                                                     DashboardNotificationsParamsMapper mapper,
                                                                     TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyTrialArrangementsNotifyOtherParty(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented()
            ? SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT.getScenario();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (caseData.isApplicantNotRepresented()) {
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
}
