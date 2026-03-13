package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
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
        if (isClaimProceedInCaseMan() || isPaperResponse()) {
            return false;
        }
        return hasResponseFullAdmit() && isPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        if (isClaimProceedInCaseMan() || isPaperResponse()) {
            return false;
        }
        return hasResponseFullAdmit() && caseData.isPayBySetDate()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        if (isClaimProceedInCaseMan() || isPaperResponse()) {
            return false;
        }
        return hasResponseFullAdmit() && caseData.isPayByInstallment()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean isEligibleForCCJ() {
        if (isPaperResponse()) {
            return false;
        }
        return CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.equals(caseData.getCcdState())
            && caseData.getRespondent1ResponseDeadline() != null
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
        if (isClaimProceedInCaseMan() || isPaperResponse()) {
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
            || (caseData.hasApplicantRejectedRepaymentPlan())
            || (CaseState.AWAITING_APPLICANT_INTENTION.equals(caseData.getCcdState())
            && isMintiClaim(caseData) && isClaimantProceeding(caseData));
    }

    private boolean isMintiClaim(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    private boolean isClaimantProceeding(CaseData caseData) {
        return (caseData.getCaseDataLiP() != null
            && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()))
            || YES.equals(caseData.getApplicant1ProceedWithClaim());
    }

    @Override
    public boolean hasClaimEnded() {
        return (Objects.nonNull(caseData.getApplicant1ProceedsWithClaimSpec())
            && caseData.getApplicant1ProceedsWithClaimSpec().equals(YesOrNo.NO)
            && caseData.isRespondentResponseFullDefence());
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
        return isSDOMadeByLegalAdviser()
                && nonNull(caseData.getOrderRequestedForReviewClaimant())
                && caseData.getOrderRequestedForReviewClaimant().equals(YES) && !isDecisionForReconsiderationMade()
                && !isSDODoneAfterDecisionForReconsiderationMade()
                && !isGeneralOrderAfterDecisionForReconsiderationMade();
    }

    @Override
    public boolean isSDOOrderInReviewOtherParty() {
        return isSDOMadeByLegalAdviser()
                && nonNull(caseData.getOrderRequestedForReviewDefendant())
                && caseData.getOrderRequestedForReviewDefendant().equals(YES)
                && !isSDOOrderInReview() && !isDecisionForReconsiderationMade()
                && !isSDODoneAfterDecisionForReconsiderationMade()
                && !isGeneralOrderAfterDecisionForReconsiderationMade();
    }

    @Override
    public boolean isDecisionForReconsiderationMade() {
        return caseData.getHearingDate() == null && caseData.getDecisionOnReconsiderationDocumentFromList().isPresent()
            && !isSDODoneAfterDecisionForReconsiderationMade()
            && !isGeneralOrderAfterDecisionForReconsiderationMade();
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return (caseData.isCcjRequestJudgmentByAdmission()
            && CaseState.All_FINAL_ORDERS_ISSUED.equals(caseData.getCcdState()))
            || Objects.nonNull(caseData.getRespondent1ResponseDeadline())
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

    public boolean isHwFEvent(CaseEvent hwfEvent) {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(EnumSet.of(hwfEvent));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return ((caseData.isHWFTypeHearing() && caseData.getCcdState() == CaseState.HEARING_READINESS)
            || (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED && caseData.isHWFTypeClaimIssued()))
            && caseData.getHwFEvent() == hwfEvent
            && !isHwfPaymentOutcome()
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    @Override
    public boolean isHwFClaimSubmit() {
        return caseData.isHWFTypeClaimIssued()
            && caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED
            && null == caseData.getHwFEvent();
    }

    @Override
    public boolean isHwFMoreInformationNeeded() {
        return isHwFEvent(CaseEvent.MORE_INFORMATION_HWF);
    }

    @Override
    public boolean isHwfNoRemission() {
        return isHwFEvent(CaseEvent.NO_REMISSION_HWF);
    }

    @Override
    public boolean isHwfPartialRemission() {
        return isHwFEvent(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED);
    }

    @Override
    public boolean isHwfFullRemission() {
        return isHwFEvent(CaseEvent.FULL_REMISSION_HWF);
    }

    @Override
    public boolean isHwfUpdatedRefNumber() {
        return isHwFEvent(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER);
    }

    @Override
    public boolean isHwfInvalidRefNumber() {
        return isHwFEvent(CaseEvent.INVALID_HWF_REFERENCE);
    }

    @Override
    public boolean isHwfPaymentOutcome() {
        Optional<LocalDateTime> eventTime;
        Optional<LocalDateTime> orderTime;
        if (Optional.ofNullable(caseData.getHearingFeePaymentDetails())
            .map(p -> p.getStatus() == PaymentStatus.SUCCESS).orElse(
                Boolean.FALSE) || caseData.hearingFeePaymentDoneWithHWF()) {
            return CaseState.HEARING_READINESS.equals(caseData.getCcdState())
                && (eventTime = getTimeOfMostRecentEventOfType(
                EnumSet.of(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT, CaseEvent.FEE_PAYMENT_OUTCOME))).isPresent()
                && ((orderTime = getTimeOfLastNonSDOOrder()).isEmpty() || eventTime.get()
                .isAfter(orderTime.get()));
        }
        return false;
    }

    @Override
    public boolean pausedForTranslationAfterResponse() {
        if (!featureToggleService.isWelshEnabledForMainCase() && (caseData.isClaimUnderTranslationAfterDefResponse() && caseData.isRespondentResponseBilingual())
            || (caseData.isClaimUnderTranslationAfterClaimantResponse() && caseData.isClaimantBilingual())) {
            return true;
        } else {
            return featureToggleService.isWelshEnabledForMainCase()
                && (caseData.isClaimUnderTranslationAfterDefResponse() || caseData.isClaimUnderTranslationAfterClaimantResponse())
                && (caseData.isRespondentResponseBilingual() || caseData.isClaimantBilingual());
        }
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
        return caseData.getCcdState() == CaseState.CASE_DISMISSED && isNull(caseData.getCaseDismissedHearingFeeDueDate());
    }

    @Override
    public boolean isCaseStayed() {
        return caseData.getCcdState() == CaseState.CASE_STAYED;
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

        return caseData.isFastTrackClaim()
            && caseData.getTrialReadyApplicant() != null
            && ((nonNull(caseData.getListingOrRelisting()) && !ListingOrRelisting.RELISTING.equals(
                caseData.getListingOrRelisting())) || isAutomaticHearingNotModifiedAfterTrialNotified())
            && (CaseState.HEARING_READINESS.equals(caseData.getCcdState()) || CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getCcdState()))
            && !isBundleCreatedStatusActive()
            && isHearingLessThanDaysAway(DAY_LIMIT)
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    private boolean isAutomaticHearingNotModifiedAfterTrialNotified() {
        Optional<LocalDateTime> automaticHearingRequested = this.getWhenWasHearingScheduled();
        Optional<LocalDateTime> trialReadyDocumentCreated = Optional.ofNullable(caseData.getClaimantTrialReadyDocumentCreated());

        return isNull(caseData.getListingOrRelisting())
            && automaticHearingRequested.isPresent()
            && (trialReadyDocumentCreated.isEmpty() || trialReadyDocumentCreated.get().isAfter(automaticHearingRequested.get()));
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

    @Override
    public boolean isTrialArrangementStatusActive() {
        Optional<LocalDate> hearingDate = getHearingDate();
        if (caseData.isFastTrackClaim()
            && (CaseState.HEARING_READINESS.equals(caseData.getCcdState()) || CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getCcdState()))
            && hearingDate.isPresent()
            && YesOrNo.YES.equals(caseData.getTrialReadyNotified())
            && isHearingLessThanDaysAway(DAY_LIMIT)
            && !isBundleCreatedStatusActive()
            && Objects.isNull(caseData.getTrialReadyChecked())
            && caseData.getTrialReadyApplicant() == null) {
            Optional<LocalDateTime> lastOrder = getTimeOfLastNonSDOOrder();
            return lastOrder.isEmpty()
                || hearingDate.get().minusDays(DAY_LIMIT)
                .isAfter(lastOrder.get().toLocalDate());
        } else {
            return false;
        }
    }

    @Override
    public boolean isCasedDiscontinued() {
        return false;
    }
}
