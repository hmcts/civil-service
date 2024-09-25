package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
public class CcdDashboardClaimantClaimMatcher extends CcdDashboardClaimMatcher implements Claim {

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);

    public CcdDashboardClaimantClaimMatcher(CaseData caseData, FeatureToggleService featureToggleService, List<CaseEventDetail> eventHistory) {
        super(caseData, featureToggleService, eventHistory);
    }

    @Override
    public boolean isClaimSubmittedNotPaidOrFailedNotHwF() {
        return caseData.isApplicantNotRepresented() && !caseData.isHWFTypeClaimIssued()
            && ((caseData.getClaimIssuedPaymentDetails() == null
            && caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED)
            || (caseData.getClaimIssuedPaymentDetails() != null
            && caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED));
    }

    @Override
    public boolean isClaimSubmittedWaitingTranslatedDocuments() {
        return caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isClaimantBilingual()
            && (caseData.getIssueDate() != null || caseData.isHWFOutcomeReady());
    }

    @Override
    public boolean hasResponsePending() {
        return caseData.getRespondent1ResponseDate() == null && !isPaperResponse()
            && caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isAfter(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    public boolean hasResponsePendingOverdue() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.hasBreathingSpace();
    }

    @Override
    public boolean hasResponseDueToday() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().toLocalDate().isEqual(LocalDate.now())
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    public boolean hasResponseFullAdmit() {
        return caseData.getRespondent1ClaimResponseTypeForSpec() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately() {
        if (isClaimProceedInCaseMan()) {
            return false;
        }
        return hasResponseFullAdmit() && isPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        if (isClaimProceedInCaseMan()) {
            return false;
        }
        return hasResponseFullAdmit() && caseData.isPayBySetDate()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        if (isClaimProceedInCaseMan()) {
            return false;
        }
        return hasResponseFullAdmit() && caseData.isPayByInstallment()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean isEligibleForCCJ() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.getPaymentTypeSelection() == null;
    }

    @Override
    public boolean claimantConfirmedDefendantPaid() {
        return caseData.getRespondent1CourtOrderPayment() != null && caseData.respondent1PaidInFull();
    }

    @Override
    public boolean isSentToCourt() {
        return false;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement() {
        return (null != caseData.getCcjPaymentDetails()
            && null != caseData.getCcjPaymentDetails().getCcjJudgmentStatement());
    }

    @Override
    public boolean isWaitingForClaimantToRespond() {
        if (isClaimProceedInCaseMan()) {
            return false;
        }
        return RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent1ClaimResponseTypeForSpec()
            && caseData.getApplicant1ResponseDate() == null;
    }

    @Override
    public boolean isProceedOffline() {
        return false;
    }

    @Override
    public boolean isPaperResponse() {
        if (!featureToggleService.isLipVLipEnabled()) {
            return false;
        }

        return Objects.nonNull(caseData.getTakenOfflineDate()) && Objects.nonNull(caseData.getCcdState())
            && caseData.getCcdState().equals(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
    }

    @Override
    public boolean hasChangeRequestFromDefendant() {
        return false;
    }

    @Override
    public boolean hasChangeRequestedFromClaimant() {
        return false;
    }

    @Override
    public boolean isPassedToCountyCourtBusinessCentre() {
        return false;
    }

    @Override
    public boolean hasClaimantAskedToSignSettlementAgreement() {
        return false;
    }

    @Override
    public boolean hasClaimantAcceptedPartialAdmissionAmount() {
        return hasDefendantStatedTheyPaid() && caseData.isResponseAcceptedByClaimant();
    }

    @Override
    public boolean haveBothPartiesSignedSettlementAgreement() {
        return false;
    }

    @Override
    public boolean hasCCJByRedetermination() {
        return caseData.hasApplicantAcceptedRepaymentPlan();
    }

    @Override
    public boolean hasDefendantStatedTheyPaid() {
        return defendantRespondedWithPartAdmit() && isPayImmediately()
            && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    private boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY
            == caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        if (isClaimProceedInCaseMan()) {
            return false;
        }
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    @Override
    public boolean hasSdoBeenDrawn() {
        return caseData.getSDODocument().isPresent();
    }

    @Override
    public boolean isBeforeHearing() {
        return isBeforeSmallClaimHearing() || (isBeforeFastTrackHearing() || noHearingScheduled());
    }

    private boolean noHearingScheduled() {
        return caseData.getSmallClaimsHearing() == null && caseData.getFastTrackHearingTime() == null;
    }

    private boolean isBeforeSmallClaimHearing() {
        return Optional.ofNullable(caseData.getSmallClaimsHearing()).map(SmallClaimsHearing::getDateFrom).map(
            hearingFromDate -> hearingFromDate.isAfter(LocalDate.now())).orElse(false);
    }

    private boolean isBeforeFastTrackHearing() {
        return Optional.ofNullable(caseData.getFastTrackHearingTime()).map(FastTrackHearingTime::getDateFrom).map(
            hearingFromDate -> hearingFromDate.isAfter(LocalDate.now())).orElse(false);
    }

    @Override
    public boolean isMediationSuccessful() {
        return !hasSdoBeenDrawn() && Objects.nonNull(caseData.getMediation())
            && Objects.nonNull(caseData.getMediation().getMediationSuccessful())
            && Objects.nonNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isMediationUnsuccessful() {
        return !hasSdoBeenDrawn() && Objects.nonNull(caseData.getMediation())
            && ((Objects.nonNull(caseData.getMediation().getUnsuccessfulMediationReason())
            && !caseData.getMediation().getUnsuccessfulMediationReason().isEmpty())
            || (Objects.nonNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect())
            && !caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect().isEmpty()));
    }

    @Override
    public boolean isMediationPending() {
        return Objects.nonNull(caseData.getCcdState()) && caseData.getCcdState().equals(CaseState.IN_MEDIATION)
            && Objects.nonNull(caseData.getMediation())
            && Objects.nonNull(caseData.getMediation().getMediationSuccessful())
            && Objects.isNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isCourtReviewing() {
        return (!hasSdoBeenDrawn() && (caseData.isRespondentResponseFullDefence() || caseData.isPartAdmitClaimSpec())
            && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState()))
            || (caseData.hasApplicantRejectedRepaymentPlan());
    }

    @Override
    public boolean hasClaimEnded() {
        return (Objects.nonNull(caseData.getApplicant1ProceedsWithClaimSpec())
            && caseData.getApplicant1ProceedsWithClaimSpec().equals(YesOrNo.NO)
            && caseData.isRespondentResponseFullDefence()) || caseData.getApplicant1ResponseDeadlinePassed();
    }

    @Override
    public boolean isClaimRejectedAndOfferSettleOutOfCourt() {
        return false;
    }

    @Override
    public boolean claimantAcceptedOfferOutOfCourt() {
        return false;
    }

    @Override
    public boolean hasClaimantRejectOffer() {
        return false;
    }

    @Override
    public boolean isPartialAdmissionRejected() {
        return CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState())
            && caseData.isPartAdmitClaimSpec() && NO.equals(caseData.getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @Override
    public boolean isSDOOrderInReview() {
        return featureToggleService.isCaseProgressionEnabled() && isSDOMadeByLegalAdviser()
            && nonNull(caseData.getOrderRequestedForReviewClaimant())
            && caseData.getOrderRequestedForReviewClaimant().equals(YES) && !isDecisionForReconsiderationMade();
    }

    @Override
    public boolean isSDOOrderInReviewOtherParty() {
        return featureToggleService.isCaseProgressionEnabled() && isSDOMadeByLegalAdviser()
            && nonNull(caseData.getOrderRequestedForReviewDefendant())
            && caseData.getOrderRequestedForReviewDefendant().equals(YES)
            && !isSDOOrderInReview() && !isDecisionForReconsiderationMade();
    }

    @Override
    public boolean isDecisionForReconsiderationMade() {
        return caseData.getHearingDate() == null && caseData.getDecisionOnReconsiderationDocumentFromList().isPresent();
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.getPaymentTypeSelection() != null;
    }

    @Override
    public boolean isPartialAdmissionAccepted() {
        return caseData.isPartAdmitClaimSpec() && caseData.isPartAdmitClaimNotSettled()
            && caseData.isPayImmediately() && YES == caseData.getApplicant1AcceptAdmitAmountPaidSpec();
    }

    @Override
    public boolean isPaymentPlanRejected() {
        return ((caseData.isPartAdmitClaimSpec() || caseData.isFullAdmitClaimSpec())
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && caseData.hasApplicantRejectedRepaymentPlan() && !isIndividualORSoleTrader());
    }

    @Override
    public boolean isPaymentPlanRejectedRequestedJudgeDecision() {
        return ((caseData.isPartAdmitClaimSpec() || caseData.isFullAdmitClaimSpec())
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && caseData.hasApplicantRejectedRepaymentPlan() && isIndividualORSoleTrader() && isCourtDecisionRejected());
    }

    private boolean isCourtDecisionRejected() {
        return Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .map(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
            .orElse(Boolean.FALSE);
    }

    private boolean isIndividualORSoleTrader() {
        return nonNull(caseData.getRespondent1()) && caseData.getRespondent1().isIndividualORSoleTrader();
    }

    @Override
    public boolean isHwFClaimSubmit() {
        return caseData.isHWFTypeClaimIssued()
            && caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED
            && null == caseData.getHwFEvent();
    }

    @Override
    public boolean isHwFMoreInformationNeeded() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(EnumSet.of(CaseEvent.MORE_INFORMATION_HWF));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return (caseData.isHWFTypeHearing()
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == CaseEvent.MORE_INFORMATION_HWF
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwfNoRemission() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(EnumSet.of(CaseEvent.NO_REMISSION_HWF));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return (caseData.isHWFTypeHearing()
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == CaseEvent.NO_REMISSION_HWF
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwfPartialRemission() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(
            EnumSet.of(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return (caseData.isHWFTypeHearing()
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == CaseEvent.PARTIAL_REMISSION_HWF_GRANTED
            && (eventTime).isPresent()
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwfUpdatedRefNumber() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(
            EnumSet.of(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return (caseData.isHWFTypeHearing()
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwfInvalidRefNumber() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(EnumSet.of(CaseEvent.INVALID_HWF_REFERENCE));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return (caseData.isHWFTypeHearing()
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == CaseEvent.INVALID_HWF_REFERENCE
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwfPaymentOutcome() {
        Optional<LocalDateTime> eventTime;
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        if (Optional.ofNullable(caseData.getHearingFeePaymentDetails())
            .map(p -> p.getStatus() == PaymentStatus.SUCCESS).orElse(
                Boolean.FALSE)) {
            return ((eventTime = getTimeOfMostRecentEventOfType(
                EnumSet.of(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT))).isPresent())
                && ((orderTime = getTimeOfLastNonSDOOrder()).isEmpty() || eventTime.get()
                .isAfter(orderTime.get()));
        }
        return (caseData.isHWFTypeHearing()
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == CaseEvent.FULL_REMISSION_HWF
            && ((eventTime = getTimeOfMostRecentEventOfType(EnumSet.of(CaseEvent.FULL_REMISSION_HWF))).isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean defendantRespondedWithPreferredLanguageWelsh() {
        return caseData.isRespondentResponseBilingual()
            && caseData.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    public boolean isWaitingForClaimantIntentDocUpload() {
        return caseData.isRespondentResponseFullDefence() && caseData.getApplicant1ResponseDate() != null
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION && caseData.isClaimantBilingual();
    }

    public boolean isNocForDefendant() {
        return isPaperResponse() && (caseData.getBusinessProcess() != null
            && CaseEvent.APPLY_NOC_DECISION_DEFENDANT_LIP.name()
            .equals(caseData.getBusinessProcess().getCamundaEvent()));
    }

    public boolean isDefaultJudgementIssued() {
        return false;
    }

    @Override
    public boolean isCaseDismissed() {
        return caseData.getCcdState() == CaseState.CASE_DISMISSED;
    }

    @Override
    public boolean isCaseStayed() {
        return caseData.getCcdState() == CaseState.CASE_STAYED;
    }

    @Override
    public boolean isHearingScheduled() {
        return caseData.getHearingDate() != null;
    }

    @Override
    public boolean isAwaitingJudgment() {
        return caseData.getCcdState() == CaseState.DECISION_OUTCOME;
    }

    @Override
    public boolean trialArrangementsSubmitted() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(
            EnumSet.of(CaseEvent.GENERATE_TRIAL_READY_FORM_APPLICANT));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return caseData.getTrialReadyApplicant() == YesOrNo.YES
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwFHearingSubmit() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(
            EnumSet.of(CaseEvent.APPLY_HELP_WITH_HEARING_FEE));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return caseData.isHWFTypeHearing() && caseData.getHwFEvent() == null
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }
}
