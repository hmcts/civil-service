package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@Service
public class CreateClaimAfterPaymentDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public CreateClaimAfterPaymentDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                           DashboardNotificationsParamsMapper mapper,
                                                           FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyDefendant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (!isEligible(caseData)) {
            return Map.of();
        }

        Map<String, Boolean> scenarios = new HashMap<>();
        AllocatedTrack allocatedTrack = AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null);
        boolean isUnrepresented = caseData.isRespondent1NotRepresented();

        scenarios.put(SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(), isUnrepresented);
        scenarios.put(SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT.getScenario(),
                      isUnrepresented && FAST_CLAIM.equals(allocatedTrack));
        scenarios.put(SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
                      isUnrepresented && featureToggleService.isLipQueryManagementEnabled(caseData));
        scenarios.put(SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
                      isUnrepresented && featureToggleService.isLipQueryManagementEnabled(caseData));
        return scenarios;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return isEligible(caseData);
    }

    private boolean isEligible(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && featureToggleService.isDashboardEnabledForCase(caseData)
            && LipPredicate.caseContainsLiP.test(caseData)
            && caseData.isRespondent1NotRepresented();
    }
}
