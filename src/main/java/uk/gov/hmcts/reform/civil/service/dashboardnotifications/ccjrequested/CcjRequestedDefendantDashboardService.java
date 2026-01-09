package uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT;

@Service
public class CcjRequestedDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public CcjRequestedDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
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
        if (respondentRejectedSettlementAgreementOrNotRespondedByDeadline(caseData)) {
            return SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT
                .getScenario();
        } else if (respondentAcceptedSettlementAgreementButMissedPayment(caseData)) {
            return SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT
                .getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled();
    }

    private boolean respondentRejectedSettlementAgreementOrNotRespondedByDeadline(CaseData caseData) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && (caseData.isRespondentRejectedSettlementAgreement()
            || (!caseData.isRespondentRespondedToSettlementAgreement() && caseData.isSettlementAgreementDeadlineExpired()));
    }

    private boolean respondentAcceptedSettlementAgreementButMissedPayment(CaseData caseData) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && caseData.isRespondentSignedSettlementAgreement();
    }
}
