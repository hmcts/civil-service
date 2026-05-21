package uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_GRANTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType.DEFAULT_JUDGMENT;

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
        return isDefaultJudgmentGranted(caseData)
            ? SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_GRANTED_CLAIMANT.getScenario()
            : SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT.getScenario();
    }

    private boolean isDefaultJudgmentGranted(CaseData caseData) {
        return featureToggleService.isJudgmentBufferEnabled()
            && caseData != null
            && CaseState.All_FINAL_ORDERS_ISSUED.equals(caseData.getCcdState())
            && Optional.ofNullable(caseData.getActiveJudgment())
            .map(activeJudgment -> DEFAULT_JUDGMENT.equals(activeJudgment.getType())
                && JudgmentState.ISSUED.equals(activeJudgment.getState()))
            .orElse(false);
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }
}
