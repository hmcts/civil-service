package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Using DashboardClaimStatusFactory means the order of the values of this enum reflect priority.
 */
public enum DashboardClaimStatus {

    CASE_DISMISSED(
        Claim::isCaseDismissed
    ),
    CASE_STAYED(Claim::isCaseStayed),
    DEFENDANT_APPLY_NOC(
        Claim::isNocForDefendant
    ),
    HEARING_FEE_UNPAID(
        Claim::isCaseStruckOut
    ),
    MEDIATION_UNSUCCESSFUL(
        Claim::isMediationUnsuccessful
    ),
    MEDIATION_SUCCESSFUL(
        Claim::isMediationSuccessful
    ),
    CLAIMANT_REJECT_PARTIAL_ADMISSION(
        Claim::isPartialAdmissionRejected
    ),
    CLAIMANT_ACCEPTED_STATES_PAID(
        Claim::claimantConfirmedDefendantPaid
    ),
    CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT(
        Claim::hasClaimantAcceptedPartialAdmissionAmount
    ),
    SDO_ORDER_CREATED(
        Claim::isSDOOrderCreated
    ),
    SDO_ORDER_LEGAL_ADVISER_CREATED(
        Claim::isSDOOrderLegalAdviserCreated
    ),
    SDO_ORDER_IN_REVIEW(
        Claim::isSDOOrderInReview
    ),
    SDO_ORDER_IN_REVIEW_OTHER_PARTY(
        Claim::isSDOOrderInReviewOtherParty
    ),
    DECISION_FOR_RECONSIDERATION_MADE(
        Claim::isDecisionForReconsiderationMade
    ),
    AWAITING_JUDGMENT(
        Claim::isAwaitingJudgment
    ),
    BUNDLE_CREATED(
        c -> {
            Optional<LocalDateTime> bundleDate;
            Optional<LocalDateTime> lastOrderDate;
            return c.isHearingScheduled()
                && c.isHearingLessThanDaysAway(3 * 7)
                && (bundleDate = c.getBundleCreationDate()).isPresent()
                && (
                (lastOrderDate = c.getTimeOfLastNonSDOOrder()).isEmpty()
                    || lastOrderDate.get().isBefore(bundleDate.get())
            );
        }
    ),
    TRIAL_ARRANGEMENTS_SUBMITTED(
        Claim::trialArrangementsSubmitted
    ),
    TRIAL_ARRANGEMENTS_REQUIRED(
        c -> {
            // same day amount than TRIAL_OR_HEARING_SCHEDULED
            int dayLimit = 6 * 7;
            Optional<LocalDate> hearingDate = c.getHearingDate();
            if (hearingDate.isPresent()
                && LocalDate.now().plusDays(dayLimit + 1).isAfter(hearingDate.get())) {
                Optional<LocalDateTime> lastOrder = c.getTimeOfLastNonSDOOrder();
                if (lastOrder.isPresent()) {
                    LocalDate trigger = hearingDate.get().minusDays(dayLimit);
                    return trigger.isAfter(lastOrder.get().toLocalDate());
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    ),
    CLAIMANT_HWF_FEE_PAYMENT_OUTCOME(
        Claim::isHwfPaymentOutcome
    ),
    CLAIMANT_HWF_NO_REMISSION(
        Claim::isHwfNoRemission
    ),
    CLAIMANT_HWF_PARTIAL_REMISSION(
        Claim::isHwfPartialRemission
    ),
    CLAIMANT_HWF_UPDATED_REF_NUMBER(
        Claim::isHwfUpdatedRefNumber
    ),
    CLAIMANT_HWF_INVALID_REF_NUMBER(
        Claim::isHwfInvalidRefNumber
    ),
    CLAIM_SUBMIT_HWF(
        Claim::isHwFClaimSubmit
    ),
    HWF_MORE_INFORMATION_NEEDED(
        Claim::isHwFMoreInformationNeeded
    ),
    HEARING_SUBMIT_HWF(
        Claim::isHwFHearingSubmit
    ),
    TRIAL_OR_HEARING_SCHEDULED(
        c -> {
            Optional<LocalDateTime> hearingScheduledDate;
            Optional<LocalDateTime> orderDate;
            if (c.isHearingScheduled()
                && !c.isHearingLessThanDaysAway(6 * 7)
                && ((hearingScheduledDate = c.getWhenWasHearingScheduled()).isPresent())
            ) {
                return ((orderDate = c.getTimeOfLastNonSDOOrder()).isEmpty()
                    || orderDate.get().isBefore(hearingScheduledDate.get()));
            } else {
                return false;
            }
        }
    ),
    MORE_DETAILS_REQUIRED(
        Claim::isMoreDetailsRequired
    ),
    IN_MEDIATION(
        Claim::isMediationPending
    ),
    WAITING_FOR_CLAIMANT_INTENT_DOC_UPLOAD(
        Claim::isWaitingForClaimantIntentDocUpload
    ),
    CLAIM_ENDED(
        Claim::hasClaimEnded
    ),
    CLAIMANT_REJECTED_PAYMENT_PLAN(
        Claim::isPaymentPlanRejected
    ),
    CLAIMANT_REJECTED_PAYMENT_PLAN_REQ_JUDGE_DECISION(
        Claim::isPaymentPlanRejectedRequestedJudgeDecision
    ),
    WAITING_COURT_REVIEW(
        Claim::isCourtReviewing
    ),
    TRANSFERRED(
        Claim::isSentToCourt
    ),
    CLAIMANT_AND_DEFENDANT_SIGNED_SETTLEMENT_AGREEMENT(
        Claim::hasClaimantAndDefendantSignedSettlementAgreement
    ),
    DEFENDANT_REJECTED_SETTLEMENT_AGREEMENT(
        Claim::hasDefendantRejectedSettlementAgreement
    ),
    CLAIMANT_SIGNED_SETTLEMENT_AGREEMENT_DEADLINE_EXPIRED(
        Claim::hasClaimantSignedSettlementAgreementAndDeadlineExpired
    ),
    CLAIMANT_SIGNED_SETTLEMENT_AGREEMENT(
        Claim::hasClaimantSignedSettlementAgreement
    ),
    SETTLED(
        Claim::isSettled
    ),
    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        Claim::claimantRequestedCountyCourtJudgement
    ),
    CLAIMANT_DOCUMENTS_BEING_TRANSLATED(
        Claim::defendantRespondedWithPreferredLanguageWelsh
    ),
    DEFENDANT_PART_ADMIT_PAID(
        Claim::hasDefendantStatedTheyPaid
    ),
    DEFENDANT_PART_ADMIT(
        Claim::defendantRespondedWithPartAdmit
    ),
    SETTLEMENT_SIGNED(
        Claim::haveBothPartiesSignedSettlementAgreement
    ),
    CLAIMANT_ACCEPTED_OFFER_OUT_OF_COURT(
        Claim::claimantAcceptedOfferOutOfCourt
    ),
    CLAIMANT_ASKED_FOR_SETTLEMENT(
        Claim::hasClaimantAskedToSignSettlementAgreement
    ),
    HEARING_FORM_GENERATED(Claim::isHearingFormGenerated),
    REQUESTED_CCJ_BY_REDETERMINATION(
        Claim::hasCCJByRedetermination
    ),
    DEFAULT_JUDGEMENT(
        Claim::isClaimantDefaultJudgement
    ),
    RESPONSE_OVERDUE(
        Claim::hasResponsePendingOverdue
    ),
    RESPONSE_DUE_NOW(
        Claim::hasResponseDueToday
    ),
    ADMIT_PAY_IMMEDIATELY(
        Claim::defendantRespondedWithFullAdmitAndPayImmediately
    ),
    ADMIT_PAY_BY_SET_DATE(
        Claim::defendantRespondedWithFullAdmitAndPayBySetDate
    ),
    ADMIT_PAY_INSTALLMENTS(
        Claim::defendantRespondedWithFullAdmitAndPayByInstallments
    ),
    MORE_TIME_REQUESTED(
        Claim::hasResponseDeadlineBeenExtended
    ),
    CLAIM_SUBMITTED_NOT_PAID_OR_FAILED(
        Claim::isClaimSubmittedNotPaidOrFailedNotHwF
    ),
    CLAIM_SUBMITTED_WAITING_TRANSLATED_DOCUMENTS(
        Claim::isClaimSubmittedWaitingTranslatedDocuments
    ),
    NO_RESPONSE(
        Claim::hasResponsePending
    ),
    PROCEED_OFFLINE(
        Claim::isProceedOffline
    ),
    CHANGE_BY_DEFENDANT(
        Claim::hasChangeRequestFromDefendant
    ),
    CHANGE_BY_CLAIMANT(
        Claim::hasChangeRequestedFromClaimant
    ),
    CLAIMANT_REJECT_OFFER_OUT_OF_COURT(
        Claim::hasClaimantRejectOffer
    ),
    CLAIM_REJECTED_OFFER_SETTLE_OUT_OF_COURT(
        Claim::isClaimRejectedAndOfferSettleOutOfCourt
    ),
    WAITING_FOR_CLAIMANT_TO_RESPOND(
        Claim::isWaitingForClaimantToRespond
    ),
    PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE(
        Claim::isPassedToCountyCourtBusinessCentre
    ),
    CLAIMANT_ACCEPTED_PARTIAL_ADMISSION(
        Claim::isPartialAdmissionAccepted
    ),
    ELIGIBLE_FOR_CCJ(
        Claim::isEligibleForCCJ
    ),
    RESPONSE_BY_POST(
        Claim::isPaperResponse
    ),
    DEFAULT_JUDGEMENT_ISSUED(Claim::isDefaultJudgementIssued),
    NO_STATUS(c -> false),
    ORDER_MADE(
        Claim::isOrderMade
    );

    @Getter
    private final Predicate<Claim> claimMatcher;

    DashboardClaimStatus(Predicate<Claim> claimMatcher) {
        this.claimMatcher = claimMatcher;
    }
}
