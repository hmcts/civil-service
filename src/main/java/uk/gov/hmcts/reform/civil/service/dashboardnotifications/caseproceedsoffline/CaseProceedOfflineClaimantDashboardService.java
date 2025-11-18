package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Map;

@Service
public class CaseProceedOfflineClaimantDashboardService extends CaseProceedOfflineDashboardService {

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
        return scenarioService.resolveAdditionalScenarios(caseData);
    }

    @Override
    protected boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        return scenarioService.shouldRecordScenarioInCaseProgression(caseData);
    }

    @Override
    protected String citizenRole() {
        return "CLAIMANT";
    }

    @Override
    protected boolean eligibleForCasemanState(CaseData caseData) {
        return caseData.isLipvLipOneVOne() || caseData.isLipvLROneVOne();
    }

    @Override
    protected boolean eligibleForCaseProgressionState(CaseData caseData) {
        return caseData.isLipvLipOneVOne();
    }
}
