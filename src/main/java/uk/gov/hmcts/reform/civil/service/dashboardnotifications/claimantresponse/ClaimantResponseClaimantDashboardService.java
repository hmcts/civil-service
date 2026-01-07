package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT;

@Service
public class ClaimantResponseClaimantDashboardService extends AbstractClaimantResponseDashboardService {

    public ClaimantResponseClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService,
                                                    DashboardNotificationService dashboardNotificationService,
                                                    TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService, dashboardNotificationService, taskListService);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        String scenario = resolveMintiScenario(caseData, SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario());
        if (scenario != null) {
            return scenario;
        }
        if (caseData.getCcdState() == CASE_SETTLED) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario();
        }
        if (caseData.getCcdState() == JUDICIAL_REFERRAL) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario();
        }
        scenario = resolveMediationScenario(caseData,
            SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM.getScenario(),
            SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario());
        if (scenario != null) {
            return scenario;
        }
        if (caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario();
        }
        if (hasClaimantRejectedCourtDecision(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario();
        }
        scenario = resolveClaimantEndsClaimScenario(
            caseData,
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT.getScenario()
        );
        if (scenario != null) {
            return scenario;
        }

        scenario = resolveRejectRepaymentScenario(
            caseData,
            false,
            SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario(),
            SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()
        );
        if (scenario != null) {
            return scenario;
        }
        if (caseData.isPartAdmitImmediatePaymentClaimSettled()) {
            return SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario();
        }
        return null;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    protected String citizenRole() {
        return CLAIMANT_ROLE;
    }

    @Override
    protected String generalApplicationInactiveScenario() {
        return SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario();
    }

}
