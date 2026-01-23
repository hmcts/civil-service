package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClaimIssueClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public ClaimIssueClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                               DashboardNotificationsParamsMapper mapper,
                                               FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimIssue(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled();
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>();
        if (featureToggleService.isLipVLipEnabled()) {
            if (featureToggleService.isLipQueryManagementEnabled(caseData)) {
                scenarios.put(DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(), true);
                scenarios.put(DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(), true);
            }

            if (caseData.isHWFTypeClaimIssued() && caseData.claimIssueFullRemissionNotGrantedHWF()) {
                scenarios.put(DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT.getScenario(), true);
            }
        }
        return scenarios;
    }
}
