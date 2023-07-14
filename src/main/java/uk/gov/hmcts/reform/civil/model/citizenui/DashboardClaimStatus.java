package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;

import java.util.function.Predicate;

public enum DashboardClaimStatus {

    SETTLED(
        Claim::isSettled
    ),
    DEFENDANT_PART_ADMIT_PAID(
        Claim::hasDefendantStatedTheyPaid
    ),
    DEFENDANT_PART_ADMIT(
        Claim::defendantRespondedWithPartAdmit
    ),
    CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT(
        Claim::hasClaimantAcceptedPartialAdmissionAmount
    ),
    CLAIMANT_ASKED_FOR_SETTLEMENT(
        Claim::hasClaimantAskedToSignSettlementAgreement
    ),
    SETTLEMENT_SIGNED(
        Claim::haveBothPartiesSignedSettlementAgreement
    ),
    HEARING_FORM_GENERATED(Claim::isHearingFormGenerated),

    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        Claim::claimantRequestedCountyCourtJudgement
    ),
    CLAIMANT_ACCEPTED_STATES_PAID(
        Claim::claimantConfirmedDefendantPaid
    ),
    REQUESTED_CCJ_BY_REDETERMINATION(
        Claim::hasCCJByRedetermination
    ),
    RESPONSE_OVERDUE(
        Claim::hasResponsePendingOverdue
    ),
    ELIGIBLE_FOR_CCJ(
        Claim::isEligibleForCCJ
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
    NO_RESPONSE(
        Claim::hasResponsePending
    ),
    RESPONSE_DUE_NOW(
        Claim::hasResponseDueToday
    ),
    MORE_TIME_REQUESTED(
        Claim::hasResponseDeadlineBeenExtended
    ),
    PROCEED_OFFLINE(
        Claim::isProceedOffline
    ),
    RESPONSE_BY_POST(
        Claim::isPaperResponse
    ),
    CHANGE_BY_DEFENDANT(
        Claim::hasChangeRequestFromDefendant
    ),
    CHANGE_BY_CLAIMANT(
        Claim::hasChangeRequestedFromClaimant
    ),
    WAITING_FOR_CLAIMANT_TO_RESPOND(
        Claim::isWaitingForClaimantToRespond
    ),
    PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE(
        Claim::isPassedToCountyCourtBusinessCentre
    ),
    MORE_DETAILS_REQUIRED(
        Claim::isMoreDetailsRequired
    ),
    MEDIATION_UNSUCCESSFUL(
        Claim::isMediationUnsuccessful
    ),
    MEDIATION_SUCCESSFUL(
        Claim::isMediationSuccessful
    ),
    IN_MEDIATION(
        Claim::isMediationPending
    ),
    CLAIM_ENDED(
        Claim::hasClaimEnded
    ),
    TRANSFERRED(
        Claim::isSentToCourt
    ),
    CLAIM_SENT_TO_CLAIMANT(
        Claim::claimSentToClaimant
    ),
    CLAIMANT_ACCEPTED_SETTLE_IN_COURT(
        Claim::claimantAcceptRepayment
    ),
    CLAIMANT_REJECT_OFFER(
        Claim::claimantRejectOffer
    ),
    WAITING_COURT_REVIEW(
        Claim::isCourtReviewing
    ),
    CLAIMANT_REJECT_PARTIAL_ADMISSION(
        Claim::isPartialAdmissionRejected
    ),
    NO_STATUS(c -> false);

    @Getter
    private final Predicate<Claim> claimMatcher;

    DashboardClaimStatus(Predicate<Claim> claimMatcher) {
        this.claimMatcher = claimMatcher;
    }
}
