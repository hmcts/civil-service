package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClaimIssueDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public ClaimIssueDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                DashboardNotificationsParamsMapper mapper,
                                                FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimIssue(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>();
        if (featureToggleService.isLipVLipEnabled()) {
            boolean isUnrepresented = caseData.isRespondent1NotRepresented();
            if (isUnrepresented) {
                scenarios.put(DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(), true);

                AllocatedTrack allocatedTrack = AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null);
                if (AllocatedTrack.FAST_CLAIM.equals(allocatedTrack)) {
                    scenarios.put(DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT.getScenario(), true);
                }
            }

            if (featureToggleService.isLipQueryManagementEnabled(caseData)) {
                scenarios.put(DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(), true);
                scenarios.put(DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(), true);
            }
        }
        return scenarios;
    }

}
