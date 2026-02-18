package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantsignsettlementagreement;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static java.util.Objects.nonNull;

@Service
public class DefendantSignSettlementAgreementClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public DefendantSignSettlementAgreementClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                                    DashboardNotificationsParamsMapper mapper,
                                                                    FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyDefendantSignSettlementAgreement(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return defendantRejectedSettlement(caseData)
            ? DashboardScenarios.SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_CLAIMANT.getScenario()
            : DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_DEFENDANT_RESPONSE_ACCEPTS_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && nonNull(caseData)
            && nonNull(caseData.getCaseDataLiP())
            && caseData.isApplicant1NotRepresented();
    }

    private boolean defendantRejectedSettlement(CaseData caseData) {
        CaseDataLiP caseDataLiP = caseData.getCaseDataLiP();
        return caseDataLiP != null && caseDataLiP.isDefendantSignedSettlementNotAgreed();
    }
}
