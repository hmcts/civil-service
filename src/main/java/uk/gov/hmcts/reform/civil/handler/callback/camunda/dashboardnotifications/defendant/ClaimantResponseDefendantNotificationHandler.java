package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
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
public class ClaimantResponseDefendantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationClaimantResponse";
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public ClaimantResponseDefendantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        FeatureToggleService featureToggleService,
                                                        DashboardNotificationService dashboardNotificationService,
                                                        TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        if (caseData.getCcdState() == CASE_SETTLED) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(
                caseId,
                "DEFENDANT"
            );

            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                caseId,
                "DEFENDANT"
            );
        }
        if (caseData.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM) {
            ScenarioRequestParams notificationParams = new ScenarioRequestParams(mapper.mapCaseDataToParams(caseData));
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams);
        }
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    public String getScenario(CaseData caseData) {
        return tryMinti(caseData)
            .or(() -> tryCaseSettled(caseData))
            .or(() -> tryImmediatePaymentSettled(caseData))
            .or(() -> tryCourtDecisionRejected(caseData))
            .or(() -> tryCourtDecisionInFavourOfDefendant(caseData))
            .or(() -> trySettlementAgreement(caseData))
            .or(() -> tryJudicialReferral(caseData))
            .or(() -> tryInMediation(caseData))
            .or(() -> tryClaimantRejectRepaymentPlan(caseData))
            .or(() -> tryLrvLipPartFullAdmitAndPayByPlan(caseData))
            .or(() -> tryLrvLipFullDefenceNotProceed(caseData))
            .or(() -> tryCaseStayed(caseData))
            .orElse(null);
    }

    private Optional<String> tryMinti(CaseData caseData) {
        return caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION && isMintiApplicable(caseData)
            ? Optional.of(SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryCaseSettled(CaseData caseData) {
        // CASE_SETTLED overrides everything, even settlement agreements.
        return caseData.getCcdState() == CASE_SETTLED
            && Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec())
            && caseData.isClaimantIntentionSettlePartAdmit()
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryImmediatePaymentSettled(CaseData caseData) {
        return caseData.isPartAdmitImmediatePaymentClaimSettled() || isLrvLipFullAdmitImmediatePayClaimSettled(caseData)
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryCourtDecisionRejected(CaseData caseData) {
        return isCourtDecisionRejected(caseData)
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryCourtDecisionInFavourOfDefendant(CaseData caseData) {
        return caseData.hasApplicant1CourtDecisionInFavourOfDefendant()
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> trySettlementAgreement(CaseData caseData) {
        // Settlement agreement sits above other states such as JUDICIAL_REFERRAL.
        return Optional.ofNullable(caseData)
            .filter(CaseData::hasApplicant1SignedSettlementAgreement)
            .flatMap(cd -> {
                if (cd.hasApplicant1CourtDecisionInFavourOfClaimant()) {
                    return Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT.getScenario());
                }
                if (cd.hasApplicantAcceptedRepaymentPlan()) {
                    return Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario());
                }
                return Optional.empty();
            });
    }

    private Optional<String> tryJudicialReferral(CaseData caseData) {
        return caseData.getCcdState() == CaseState.JUDICIAL_REFERRAL
            ? resolveJudicialReferralScenario(caseData)
            : Optional.empty();
    }

    private Optional<String> tryInMediation(CaseData caseData) {
        return caseData.getCcdState() == CaseState.IN_MEDIATION
            ? Optional.of(getInMediationScenario(caseData))
            : Optional.empty();
    }

    private Optional<String> tryClaimantRejectRepaymentPlan(CaseData caseData) {
        return isClaimantRejectRepaymentPlan(caseData)
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryLrvLipPartFullAdmitAndPayByPlan(CaseData caseData) {
        return isLrvLipPartFullAdmitAndPayByPlan(caseData)
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryLrvLipFullDefenceNotProceed(CaseData caseData) {
        return isLrvLipFullDefenceNotProceed(caseData)
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> tryCaseStayed(CaseData caseData) {
        return caseData.getCcdState() == CaseState.CASE_STAYED && caseData.isClaimantDontWantToProceedWithFulLDefenceFD()
            ? Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT.getScenario())
            : Optional.empty();
    }

    private Optional<String> resolveJudicialReferralScenario(CaseData caseData) {
        // Judicial referral branch maintains its own priority order.
        if (isClaimantRejectedNotPaid(caseData)) {
            return Optional.of(SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario());
        }

        if (isGoToHearingPartAdmitFullDefenceStatesPaid(caseData)) {
            return Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT.getScenario());
        }

        if (isGoToHearingDefFullDefenceClaimantDisputes(caseData)) {
            return Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario());
        }

        if (isClaimantRejectingMediation(caseData)) {
            return Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT.getScenario());
        }

        if (isGoToHearingDefendantPartAdmit(caseData)) {
            return Optional.of(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario());
        }

        return Optional.empty();
    }

    private String getInMediationScenario(CaseData caseData) {
        return getFeatureToggleService().isCarmEnabledForCase(caseData)
            ? SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM.getScenario()
            : SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario();
    }

    private boolean isMintiApplicable(CaseData caseData) {
        return getFeatureToggleService().isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    private boolean isLrvLipFullAdmitImmediatePayClaimSettled(CaseData caseData) {
        return getFeatureToggleService().isJudgmentOnlineLive()
            && !caseData.isApplicantLiP()
            && caseData.isFullAdmitPayImmediatelyClaimSpec()
            && caseData.getApplicant1ProceedWithClaim() == null;
    }

    private boolean isCourtDecisionRejected(CaseData caseData) {
        return Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .map(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
            .orElse(false);
    }

    private boolean isClaimantRejectRepaymentPlan(CaseData caseData) {
        return ((caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && (caseData.isLRvLipOneVOne()
                    || (caseData.getRespondent1() != null && caseData.getRespondent1().isCompanyOROrganisation()))
            && caseData.hasApplicantRejectedRepaymentPlan());
    }

    private boolean isLrvLipPartFullAdmitAndPayByPlan(CaseData caseData) {
        return !getFeatureToggleService().isJudgmentOnlineLive()
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

    private boolean isClaimantRejectedNotPaid(CaseData caseData) {
        return caseData.hasDefendantNotPaid()
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && (caseData.isFullDefenceNotPaid() || caseData.isClaimantIntentionNotSettlePartAdmit())
            && (caseData.getApplicant1PartAdmitConfirmAmountPaidSpec() != YesOrNo.YES)
            && caseData.isMediationRejectedOrFastTrack());
    }

    private boolean isGoToHearingPartAdmitFullDefenceStatesPaid(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1ClaimResponseTypeForSpec())
            .map(type -> switch (type) {
                case FULL_DEFENCE -> caseData.getRespondToClaim();
                case PART_ADMISSION -> caseData.getRespondToAdmittedClaim();
                default -> null;
            })
            .filter(respondToClaim -> respondToClaim.getHowMuchWasPaid() != null)
            .filter(respondToClaim -> caseData.getApplicant1PartAdmitConfirmAmountPaidSpec() == YesOrNo.YES)
            .filter(respondToClaim -> caseData.hasClaimantNotAgreedToFreeMediation()
                || caseData.hasDefendantNotAgreedToFreeMediation())
            .isPresent();
    }

    private boolean isGoToHearingDefFullDefenceClaimantDisputes(CaseData caseData) {
        return caseData.isRespondentResponseFullDefence()
            && (isNull(caseData.getResponseClaimMediationSpecRequired())
            || caseData.hasDefendantNotAgreedToFreeMediation());
    }

    private boolean isClaimantRejectingMediation(CaseData caseData) {
        if (!caseData.isRespondentResponseFullDefence()) {
            return false;
        }
        return Optional.ofNullable(caseData.getCaseDataLiP())
            .map(cdl -> {
                var mediation = cdl.getApplicant1ClaimMediationSpecRequiredLip();
                return isNull(mediation) || MediationDecision.No.equals(mediation.getHasAgreedFreeMediation());
            })
            .orElse(false);
    }

    private boolean isGoToHearingDefendantPartAdmit(CaseData caseData) {
        return Objects.nonNull(caseData.getApplicant1AcceptAdmitAmountPaidSpec())
            && caseData.isClaimantRejectsClaimAmount()
            && (caseData.hasClaimantNotAgreedToFreeMediation()
            || caseData.hasDefendantNotAgreedToFreeMediation());
    }
}
