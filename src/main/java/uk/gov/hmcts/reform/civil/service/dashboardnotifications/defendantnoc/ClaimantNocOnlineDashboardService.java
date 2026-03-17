package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT;

@Service
public class ClaimantNocOnlineDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public ClaimantNocOnlineDashboardService(DashboardScenariosService dashboardScenariosService,
                                             DashboardNotificationsParamsMapper mapper,
                                             FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isDefendantNoCOnlineForCase(caseData)
            && caseData.getCcdState() != null
            && !CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.equals(caseData.getCcdState());
    }
}
