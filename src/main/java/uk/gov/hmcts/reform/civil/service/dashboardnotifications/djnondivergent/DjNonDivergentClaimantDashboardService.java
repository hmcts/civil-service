package uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ENTERED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.DefaultJudgmentIssuedCaseDataHelper.isFinalOrdersIssuedDefaultJudgment;

@Service
public class DjNonDivergentClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public DjNonDivergentClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                  DashboardNotificationsParamsMapper mapper,
                                                  FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyDjNonDivergent(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (!featureToggleService.isJudgmentBufferEnabled()) {
            return SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT.getScenario();
        }
        return isFinalOrdersIssuedDefaultJudgment(caseData)
            ? SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ENTERED_CLAIMANT.getScenario()
            : null;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented()
            && (!featureToggleService.isJudgmentBufferEnabled() || isFinalOrdersIssuedDefaultJudgment(caseData));
    }
}
