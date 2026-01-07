package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
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
public class ClaimantResponseDefendantDashboardService extends AbstractClaimantResponseDashboardService {

    public ClaimantResponseDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                     DashboardNotificationsParamsMapper mapper,
                                                     FeatureToggleService featureToggleService,
                                                     DashboardNotificationService dashboardNotificationService,
                                                     TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService, dashboardNotificationService, taskListService);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        String scenario = resolveMintiScenario(caseData, SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario());
        if (scenario != null) {
            return scenario;
        }
        if (caseData.getCcdState() == CASE_SETTLED) {
            return getCaseSettledScenarios(caseData);
        }
        if (caseData.isPartAdmitImmediatePaymentClaimSettled() || isLrvLipFullAdmitImmediatePayClaimSettled(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario();
        }
        if (hasClaimantRejectedCourtDecision(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT
                .getScenario();
        }
        if (caseData.hasApplicant1CourtDecisionInFavourOfDefendant()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario();
        }
        if (caseData.hasApplicant1SignedSettlementAgreement() && caseData.hasApplicant1CourtDecisionInFavourOfClaimant()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT
                .getScenario();
        }
        if (caseData.hasApplicantAcceptedRepaymentPlan() && caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario();
        }
        if (isCaseStateJudicialReferral(caseData)) {
            return getJudicialReferralScenarios(caseData);
        }
        scenario = resolveMediationScenario(caseData,
            SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM.getScenario(),
            SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario());
        if (scenario != null) {
            return scenario;
        }
        scenario = resolveRejectRepaymentScenario(
            caseData,
            true,
            SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario(),
            null
        );
        if (scenario != null) {
            return scenario;
        }
        if (isLrvLipPartFullAdmitAndPayByPlan(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario();
        }
        if (isLrvLipFullDefenceNotProceed(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario();
        }
        scenario = resolveClaimantEndsClaimScenario(
            caseData,
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT.getScenario()
        );
        return scenario;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    protected String citizenRole() {
        return DEFENDANT_ROLE;
    }

    @Override
    protected String generalApplicationInactiveScenario() {
        return SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario();
    }

    private String getCaseSettledScenarios(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec())
            && caseData.isClaimantIntentionSettlePartAdmit()) {
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
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario();
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

    private static boolean isCaseStateJudicialReferral(CaseData caseData) {
        return caseData.getCcdState() == JUDICIAL_REFERRAL;
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

    private boolean getGoToHearingScenarioClaimantRejectsMediation(CaseData caseData) {
        Optional<CaseDataLiP> caseDataLip = Optional.ofNullable(caseData.getCaseDataLiP());
        return caseDataLip.filter(caseDataLiP -> caseData.isRespondentResponseFullDefence()
            && (isNull(caseDataLiP.getApplicant1ClaimMediationSpecRequiredLip())
            || (caseDataLiP.getApplicant1ClaimMediationSpecRequiredLip()
            .getHasAgreedFreeMediation().equals(MediationDecision.No)))).isPresent();
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
            && HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired());
    }

    private boolean isLrvLipFullAdmitImmediatePayClaimSettled(CaseData caseData) {
        return featureToggleService.isJudgmentOnlineLive()
            && !caseData.isApplicantLiP()
            && caseData.isFullAdmitPayImmediatelyClaimSpec()
            && caseData.getApplicant1ProceedWithClaim() == null;
    }
}
