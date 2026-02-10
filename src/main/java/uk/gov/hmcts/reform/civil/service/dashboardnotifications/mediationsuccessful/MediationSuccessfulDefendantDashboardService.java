package uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationsuccessful;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_SUCCESSFUL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_SUCCESSFUL;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import org.springframework.stereotype.Service;

@Service
public class MediationSuccessfulDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public MediationSuccessfulDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        FeatureToggleService featureToggleService) {

        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyMediationSuccessful(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            return SCENARIO_AAA6_DEFENDANT_MEDIATION_SUCCESSFUL.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_SUCCESSFUL_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }
}
