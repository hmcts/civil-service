package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseScenarioHelper.isCarmApplicable;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseScenarioHelper.scenarioForRespondentPartyType;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_BY_SET_DATE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ALREADY_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_FULL_DISPUTE_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MULTI_INT_FAST_DEFENDANT;

@Service
public class DefendantResponseDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public DefendantResponseDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                      DashboardNotificationsParamsMapper mapper,
                                                      FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyDefendantResponse(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {

        if (isCarmApplicable(featureToggleService, caseData) && isFullDefenceFullDispute(caseData)) {
            return SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM.getScenario();
        }

        if (caseData.isPayBySetDate()) {
            return scenarioForRespondentPartyType(
                caseData,
                SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_DEFENDANT.getScenario(),
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_BY_SET_DATE_DEFENDANT.getScenario()
            );
        }

        if ((caseData.isRespondentResponseFullDefence() && caseData.hasDefendantPaidTheAmountClaimed())
            || (caseData.isPartAdmitClaimSpec() && caseData.isPartAdmitAlreadyPaid())) {
            return SCENARIO_AAA6_DEFENDANT_ALREADY_PAID.getScenario();
        }

        if (!caseData.isSmallClaim() && caseData.isRespondentResponseFullDefence()
            && caseData.isClaimBeingDisputed()) {
            return SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MULTI_INT_FAST_DEFENDANT.getScenario();
        }

        if (caseData.isFullAdmitPayImmediatelyClaimSpec()
            || caseData.isPartAdmitPayImmediatelyClaimSpec()) {
            return SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT.getScenario();
        } else if (caseData.isRespondentResponseFullDefence()
            && !caseData.hasDefendantAgreedToFreeMediation()) {
            return SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_NO_MEDIATION_DEFENDANT.getScenario();
        }
        if ((caseData.isPartAdmitClaimSpec() || caseData.isFullAdmitClaimSpec())
            && caseData.isPayByInstallment()) {
            return scenarioForRespondentPartyType(
                caseData,
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT.getScenario(),
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_DEFENDANT.getScenario()
            );
        }
        if (caseData.isRespondentResponseFullDefence() && caseData.isClaimBeingDisputed()
            && caseData.hasDefendantAgreedToFreeMediation()) {
            return SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_FULL_DISPUTE_MEDIATION.getScenario();
        }

        return null;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    private boolean isFullDefenceFullDispute(CaseData caseData) {
        return caseData.isRespondentResponseFullDefence()
            && caseData.isClaimBeingDisputed();
    }
}
