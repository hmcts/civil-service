package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Optional;

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
public class ClaimantResponseClaimantDashboardService extends ClaimantResponseDashboardServiceBase {

    public ClaimantResponseClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService,
                                                    DashboardNotificationService dashboardNotificationService,
                                                    TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService, dashboardNotificationService, taskListService);
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return resolveScenario(
            () -> multiIntScenario(caseData),
            () -> caseSettledScenario(caseData),
            () -> judicialReferralScenario(caseData),
            () -> mediationScenario(caseData),
            () -> settlementAgreementScenario(caseData),
            () -> courtDecisionRejectedScenario(caseData),
            () -> claimantEndsClaimScenario(caseData),
            () -> companyRepaymentRejectedScenario(caseData),
            () -> partAdmitImmediateScenario(caseData)
        );
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled() && caseData.isApplicantNotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        clearSettledCaseNotificationsIfNeeded(caseData, CLAIMANT_ROLE);
        recordGeneralApplicationScenarioIfNeeded(
            caseData,
            authToken,
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()
        );
    }

    private boolean hasClaimantRejectedCourtDecision(CaseData caseData) {
        return
            Optional.ofNullable(caseData)
                .map(CaseData::getCaseDataLiP)
                .map(CaseDataLiP::getApplicant1LiPResponse)
                .filter(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
                .isPresent();
    }

    private boolean shouldShowMultiIntScenario(CaseData caseData) {
        return isMintiApplicable(caseData) && isCaseStateAwaitingApplicantIntention(caseData);
    }

    private String mediationScenario(CaseData caseData) {
        if (!isCaseStateInMediation(caseData)) {
            return null;
        }
        return isCarmApplicableForMediation(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM.getScenario()
            : SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario();
    }

    private boolean shouldShowClaimantEndsClaimScenario(CaseData caseData) {
        return caseData.getCcdState() == CaseState.CASE_STAYED
            && caseData.isClaimantDontWantToProceedWithFulLDefenceFD();
    }

    private boolean shouldShowCompanyRepaymentPlanRejectedScenario(CaseData caseData) {
        return (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && caseData.getRespondent1().isCompanyOROrganisation()
            && caseData.hasApplicantRejectedRepaymentPlan();
    }

    private String multiIntScenario(CaseData caseData) {
        return shouldShowMultiIntScenario(caseData)
            ? SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario()
            : null;
    }

    private String caseSettledScenario(CaseData caseData) {
        return isCaseStateSettled(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario()
            : null;
    }

    private String judicialReferralScenario(CaseData caseData) {
        return isCaseStateJudicialReferral(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario()
            : null;
    }

    private String settlementAgreementScenario(CaseData caseData) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            ? SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario()
            : null;
    }

    private String courtDecisionRejectedScenario(CaseData caseData) {
        return hasClaimantRejectedCourtDecision(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario()
            : null;
    }

    private String claimantEndsClaimScenario(CaseData caseData) {
        return shouldShowClaimantEndsClaimScenario(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT.getScenario()
            : null;
    }

    private String companyRepaymentRejectedScenario(CaseData caseData) {
        if (!shouldShowCompanyRepaymentPlanRejectedScenario(caseData)) {
            return null;
        }
        return featureToggleService.isJudgmentOnlineLive()
            ? SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario();
    }

    private String partAdmitImmediateScenario(CaseData caseData) {
        return caseData.isPartAdmitImmediatePaymentClaimSettled()
            ? SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario()
            : null;
    }
}
