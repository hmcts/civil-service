package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEEDS_OFFLINE_JUDGMENT_REQUESTED_CANCELLED_CLAIMANT;

@Service
public class CaseProceedOfflineClaimantDashboardService extends CaseProceedOfflineDashboardService {

    protected static final String CLAIMANT_ROLE = "CLAIMANT";
    private final CaseProceedOfflineClaimantScenarioService scenarioService;

    public CaseProceedOfflineClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                      DashboardNotificationsParamsMapper mapper,
                                                      DashboardNotificationService dashboardNotificationService,
                                                      TaskListService taskListService,
                                                      CaseProceedOfflineClaimantScenarioService scenarioService) {
        super(dashboardScenariosService, mapper, dashboardNotificationService, taskListService);
        this.scenarioService = scenarioService;
    }

    public void notifyCaseProceedOffline(CaseData caseData, String authToken) {
        super.notifyCaseProceedOffline(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return scenarioService.resolvePrimaryScenario(caseData);
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>(scenarioService.resolveAdditionalScenarios(caseData));
        scenarios.put(
            SCENARIO_AAA6_CASE_PROCEEDS_OFFLINE_JUDGMENT_REQUESTED_CANCELLED_CLAIMANT.getScenario(),
            isJudgmentRequested(caseData)
        );
        return scenarios;
    }

    private boolean isJudgmentRequested(CaseData caseData) {
        return CaseState.JUDGMENT_REQUESTED.equals(caseData.getPreviousCCDState());
    }

    @Override
    protected boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        return scenarioService.shouldRecordScenarioInCaseProgression(caseData);
    }

    @Override
    protected String citizenRole() {
        return CLAIMANT_ROLE;
    }

    @Override
    protected boolean eligibleForCasemanState(CaseData caseData) {
        return caseData.isLipvLipOneVOne() || caseData.isLipvLROneVOne() || caseData.isLRvLipOneVOne();
    }

    @Override
    protected boolean eligibleForCaseProgressionState(CaseData caseData) {
        return caseData.isLipvLipOneVOne() || caseData.isLipvLROneVOne() || caseData.isLRvLipOneVOne();
    }
}
