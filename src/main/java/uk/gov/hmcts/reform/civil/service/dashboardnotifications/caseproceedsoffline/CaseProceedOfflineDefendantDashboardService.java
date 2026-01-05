package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Map;

@Service
public class CaseProceedOfflineDefendantDashboardService extends CaseProceedOfflineDashboardService {

    protected static final String DEFENDANT_ROLE = "DEFENDANT";
    private final CaseProceedOfflineDefendantScenarioService scenarioService;

    public CaseProceedOfflineDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                       DashboardNotificationsParamsMapper mapper,
                                                       FeatureToggleService featureToggleService,
                                                       DashboardNotificationService dashboardNotificationService,
                                                       TaskListService taskListService,
                                                       CaseProceedOfflineDefendantScenarioService scenarioService) {
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
    protected String citizenRole() {
        return DEFENDANT_ROLE;
    }

    @Override
    protected boolean eligibleForCasemanState(CaseData caseData) {
        return caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne();
    }

    @Override
    protected boolean eligibleForCaseProgressionState(CaseData caseData) {
        return caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne();
    }

    @Override
    protected boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        return scenarioService.shouldRecordScenarioInCaseProgression(caseData);
    }
}
