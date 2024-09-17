package uk.gov.hmcts.reform.civil.model.citizenui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface Claim {

    boolean hasResponsePending();

    boolean hasResponsePendingOverdue();

    boolean hasResponseDueToday();

    boolean hasResponseFullAdmit();

    boolean defendantRespondedWithFullAdmitAndPayImmediately();

    boolean defendantRespondedWithFullAdmitAndPayBySetDate();

    boolean defendantRespondedWithFullAdmitAndPayByInstallments();

    boolean hasResponseDeadlineBeenExtended();

    boolean isEligibleForCCJ();

    boolean claimantConfirmedDefendantPaid();

    boolean isSettled();

    boolean isSentToCourt();

    boolean claimantRequestedCountyCourtJudgement();

    boolean isWaitingForClaimantToRespond();

    boolean isProceedOffline();

    boolean isPaperResponse();

    boolean hasChangeRequestFromDefendant();

    boolean hasChangeRequestedFromClaimant();

    boolean isPassedToCountyCourtBusinessCentre();

    boolean hasClaimantAskedToSignSettlementAgreement();

    boolean hasClaimantAndDefendantSignedSettlementAgreement();

    boolean hasDefendantRejectedSettlementAgreement();

    boolean hasClaimantSignedSettlementAgreement();

    boolean hasClaimantSignedSettlementAgreementAndDeadlineExpired();

    boolean hasClaimantAcceptedPartialAdmissionAmount();

    boolean haveBothPartiesSignedSettlementAgreement();

    boolean hasCCJByRedetermination();

    boolean hasDefendantStatedTheyPaid();

    boolean defendantRespondedWithPartAdmit();

    boolean isHearingFormGenerated();

    boolean hasSdoBeenDrawn();

    boolean isBeforeHearing();

    boolean isMoreDetailsRequired();

    boolean isMediationSuccessful();

    boolean isMediationUnsuccessful();

    boolean isMediationPending();

    boolean isCourtReviewing();

    boolean hasClaimEnded();

    boolean isClaimRejectedAndOfferSettleOutOfCourt();

    boolean claimantAcceptedOfferOutOfCourt();

    boolean hasClaimantRejectOffer();

    boolean isPartialAdmissionRejected();

    boolean isSDOOrderCreated();

    boolean isSDOOrderLegalAdviserCreated();

    boolean isSDOOrderInReview();

    boolean isSDOOrderInReviewOtherParty();

    boolean isDecisionForReconsiderationMade();

    boolean isClaimantDefaultJudgement();

    boolean isPartialAdmissionAccepted();

    boolean isPaymentPlanRejected();

    boolean isPaymentPlanRejectedRequestedJudgeDecision();

    boolean isHwFClaimSubmit();

    boolean isHwFMoreInformationNeeded();

    boolean isHwfNoRemission();

    boolean isHwfPartialRemission();

    boolean isHwfUpdatedRefNumber();

    boolean isHwfInvalidRefNumber();

    boolean isHwfPaymentOutcome();

    boolean defendantRespondedWithPreferredLanguageWelsh();

    boolean isWaitingForClaimantIntentDocUpload();

    boolean isClaimSubmittedNotPaidOrFailedNotHwF();

    boolean isClaimSubmittedWaitingTranslatedDocuments();

    boolean isNocForDefendant();

    boolean isCaseStruckOut();

    boolean isDefaultJudgementIssued();

    default boolean isCaseDismissed() {
        return false;
    }

    boolean isCaseStayed();

    /**
     * Hearing scheduled implies at least that hearing date is defined.
     *
     * @return true if hearing is scheduled
     */
    boolean isHearingScheduled();

    /**
     * Some statuses consider how far is the hearing date.
     *
     * @param i days
     * @return true if hearing date is defined and it is less or equal than i days away
     */
    boolean isHearingLessThanDaysAway(int i);

    boolean isAwaitingJudgment();

    boolean trialArrangementsSubmitted();

    default boolean isHwFHearingSubmit() {
        return false;
    }

    boolean isOrderMade();

    Optional<LocalDate> getHearingDate();

    Optional<LocalDateTime> getTimeOfLastNonSDOOrder();

    Optional<LocalDateTime> getBundleCreationDate();

    Optional<LocalDateTime> getWhenWasHearingScheduled();

    default boolean isBundleCreatedStatusActive() {
        Optional<LocalDateTime> bundleDate;
        Optional<LocalDateTime> lastOrderDate;
        return isHearingScheduled()
            && isHearingLessThanDaysAway(3 * 7)
            && (bundleDate = getBundleCreationDate()).isPresent()
            && (
            (lastOrderDate = getTimeOfLastNonSDOOrder()).isEmpty()
                || lastOrderDate.get().isBefore(bundleDate.get())
            );
    }

    default boolean isTrialArrangementStatusActive() {
        int dayLimit = 6 * 7;
        Optional<LocalDate> hearingDate = getHearingDate();
        if (hearingDate.isPresent()
            && LocalDate.now().plusDays(dayLimit + 1).isAfter(hearingDate.get())) {
            Optional<LocalDateTime> lastOrder = getTimeOfLastNonSDOOrder();
            return lastOrder.isEmpty()
                || hearingDate.get().minusDays(dayLimit)
                .isAfter(lastOrder.get().toLocalDate());
        } else {
            return false;
        }
    }

    default boolean isTrialScheduledStatusActive() {
        Optional<LocalDateTime> hearingScheduledDate;
        Optional<LocalDateTime> orderDate;
        if (isHearingScheduled()
            && !isHearingLessThanDaysAway(6 * 7)
            && ((hearingScheduledDate = getWhenWasHearingScheduled()).isPresent())
        ) {
            return ((orderDate = getTimeOfLastNonSDOOrder()).isEmpty()
                || orderDate.get().isBefore(hearingScheduledDate.get()));
        } else {
            return false;
        }
    }
}
