package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT;

@Service
public class ClaimantResponseDefendantDashboardService extends ClaimantResponseDashboardServiceBase {

    public ClaimantResponseDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                     DashboardNotificationsParamsMapper mapper,
                                                     FeatureToggleService featureToggleService,
                                                     DashboardNotificationService dashboardNotificationService,
                                                     TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService, dashboardNotificationService, taskListService);
    }

    public void notifyDefendant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return resolveScenario(
            () -> multiIntScenario(caseData),
            () -> caseSettledScenario(caseData),
            () -> immediatePaymentScenario(caseData),
            () -> courtDecisionRejectedScenario(caseData),
            () -> courtDecisionInFavourOfDefendantScenario(caseData),
            () -> settlementAgreementRejectedScenario(caseData),
            () -> settlementAgreementAcceptedScenario(caseData),
            () -> judicialReferralScenario(caseData),
            () -> mediationScenario(caseData),
            () -> claimantRejectRepaymentPlanScenario(caseData),
            () -> lrvLipPartFullAdmitScenario(caseData),
            () -> lrvLipFullDefenceNotProceedScenario(caseData),
            () -> claimantEndsClaimScenario(caseData)
        );
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled() && caseData.isRespondent1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        clearSettledCaseNotificationsIfNeeded(caseData, DEFENDANT_ROLE);
        recordGeneralApplicationScenarioIfNeeded(
            caseData,
            authToken,
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario()
        );
    }

    private String getCaseSettledScenarios(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec()) && caseData.isClaimantIntentionSettlePartAdmit()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario();
        }
        return null;
    }

    private String getJudicialReferralScenarios(CaseData caseData) {
        RespondToClaim respondToClaim = getRespondToClaim(caseData);
        if ((caseData.hasDefendantNotPaid()
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && (caseData.isFullDefenceNotPaid() || caseData.isClaimantIntentionNotSettlePartAdmit())
            && (caseData.getApplicant1PartAdmitConfirmAmountPaidSpec() != YesOrNo.YES))
            && caseData.isMediationRejectedOrFastTrack())) {
            return SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario();
        }
        if (Objects.nonNull(respondToClaim)
            && Objects.nonNull(respondToClaim.getHowMuchWasPaid())
            && caseData.getApplicant1PartAdmitConfirmAmountPaidSpec() == YesOrNo.YES
            && (caseData.hasClaimantNotAgreedToFreeMediation()
            || caseData.hasDefendantNotAgreedToFreeMediation())) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT
                .getScenario();
        }

        if (caseData.isRespondentResponseFullDefence()
            && (isNull(caseData.getResponseClaimMediationSpecRequired())
            || caseData.hasDefendantNotAgreedToFreeMediation())) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT
                .getScenario();
        }
        if (getGoToHearingScenarioClaimantRejectsMediation(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT
                .getScenario();
        }
        if (Objects.nonNull(caseData.getApplicant1AcceptAdmitAmountPaidSpec()) && caseData.isClaimantRejectsClaimAmount()
            && (caseData.hasClaimantNotAgreedToFreeMediation()
            || caseData.hasDefendantNotAgreedToFreeMediation())) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario();
        }
        return null;
    }

    private boolean getGoToHearingScenarioClaimantRejectsMediation(CaseData caseData) {
        Optional<CaseDataLiP> caseDataLip = Optional.ofNullable(caseData.getCaseDataLiP());
        return caseDataLip.filter(caseDataLiP -> caseData.isRespondentResponseFullDefence()
            && (isNull(caseDataLiP.getApplicant1ClaimMediationSpecRequiredLip())
            || (caseDataLiP.getApplicant1ClaimMediationSpecRequiredLip()
            .getHasAgreedFreeMediation().equals(MediationDecision.No)))).isPresent();
    }

    private boolean isCourtDecisionRejected(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        return applicant1Response != null
            && applicant1Response.hasClaimantRejectedCourtDecision();
    }

    private boolean shouldShowMultiIntScenario(CaseData caseData) {
        return isMintiApplicable(caseData) && isCaseStateAwaitingApplicantIntention(caseData);
    }

    private boolean isImmediatePaymentScenario(CaseData caseData) {
        return caseData.isPartAdmitImmediatePaymentClaimSettled()
            || isLrvLipFullAdmitImmediatePayClaimSettled(caseData);
    }

    private boolean isSettlementAgreementRejectedByCourtScenario(CaseData caseData) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && caseData.hasApplicant1CourtDecisionInFavourOfClaimant();
    }

    private boolean isSettlementAgreementAcceptedScenario(CaseData caseData) {
        return caseData.hasApplicantAcceptedRepaymentPlan()
            && caseData.hasApplicant1SignedSettlementAgreement();
    }

    private String getMediationScenario(CaseData caseData) {
        return isCarmApplicableForMediation(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM.getScenario()
            : SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario();
    }

    private boolean shouldShowClaimantEndsClaimScenario(CaseData caseData) {
        return caseData.getCcdState() == CASE_STAYED
            && caseData.isClaimantDontWantToProceedWithFulLDefenceFD();
    }

    private boolean isClaimantRejectRepaymentPlan(CaseData caseData) {
        return ((caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && (caseData.isLRvLipOneVOne() || caseData.getRespondent1().isCompanyOROrganisation())
            && caseData.hasApplicantRejectedRepaymentPlan());
    }

    private boolean isLrvLipPartFullAdmitAndPayByPlan(CaseData caseData) {
        return !featureToggleService.isJudgmentOnlineLive()
            && !caseData.isApplicantLiP()
            && caseData.hasApplicantAcceptedRepaymentPlan()
            && caseData.isCcjRequestJudgmentByAdmission();
    }

    private boolean isLrvLipFullDefenceNotProceed(CaseData caseData) {
        return !caseData.isApplicantLiP()
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            && NO.equals(caseData.getApplicant1ProceedWithClaim())
            && uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED
            .equals(caseData.getDefenceRouteRequired());
    }

    private boolean isLrvLipFullAdmitImmediatePayClaimSettled(CaseData caseData) {
        return featureToggleService.isJudgmentOnlineLive()
            && !caseData.isApplicantLiP()
            && caseData.isFullAdmitPayImmediatelyClaimSpec()
            && caseData.getApplicant1ProceedWithClaim() == null;
    }

    private RespondToClaim getRespondToClaim(CaseData caseData) {
        RespondToClaim respondToClaim = null;
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            respondToClaim = caseData.getRespondToClaim();
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            respondToClaim = caseData.getRespondToAdmittedClaim();
        }

        return respondToClaim;
    }

    private String multiIntScenario(CaseData caseData) {
        return shouldShowMultiIntScenario(caseData)
            ? SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario()
            : null;
    }

    private String caseSettledScenario(CaseData caseData) {
        return isCaseStateSettled(caseData) ? getCaseSettledScenarios(caseData) : null;
    }

    private String immediatePaymentScenario(CaseData caseData) {
        return isImmediatePaymentScenario(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario()
            : null;
    }

    private String courtDecisionRejectedScenario(CaseData caseData) {
        return isCourtDecisionRejected(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT.getScenario()
            : null;
    }

    private String courtDecisionInFavourOfDefendantScenario(CaseData caseData) {
        return caseData.hasApplicant1CourtDecisionInFavourOfDefendant()
            ? SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario()
            : null;
    }

    private String settlementAgreementRejectedScenario(CaseData caseData) {
        return isSettlementAgreementRejectedByCourtScenario(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT.getScenario()
            : null;
    }

    private String settlementAgreementAcceptedScenario(CaseData caseData) {
        return isSettlementAgreementAcceptedScenario(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario()
            : null;
    }

    private String judicialReferralScenario(CaseData caseData) {
        return isCaseStateJudicialReferral(caseData) ? getJudicialReferralScenarios(caseData) : null;
    }

    private String mediationScenario(CaseData caseData) {
        return isCaseStateInMediation(caseData) ? getMediationScenario(caseData) : null;
    }

    private String claimantRejectRepaymentPlanScenario(CaseData caseData) {
        return isClaimantRejectRepaymentPlan(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario()
            : null;
    }

    private String lrvLipPartFullAdmitScenario(CaseData caseData) {
        return isLrvLipPartFullAdmitAndPayByPlan(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario()
            : null;
    }

    private String lrvLipFullDefenceNotProceedScenario(CaseData caseData) {
        return isLrvLipFullDefenceNotProceed(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario()
            : null;
    }

    private String claimantEndsClaimScenario(CaseData caseData) {
        return shouldShowClaimantEndsClaimScenario(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT.getScenario()
            : null;
    }
}
