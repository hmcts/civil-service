package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;

import java.util.function.Predicate;

public enum DashboardClaimStatus {

    CLAIMANT_ACCEPTED_STATES_PAID(
        Claim::claimantConfirmedDefendantPaid
    ),
    SETTLED(
        Claim::isSettled
    ),
    TRANSFERRED(
        Claim::isSentToCourt
    ),
    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        Claim::claimantRequestedCountyCourtJudgement
    ),
    RESPONSE_DUE_NOW(
        Claim::hasResponseDueToday
    ),
    RESPONSE_OVERDUE(
        Claim::hasResponsePendingOverdue
    ),
    ELIGIBLE_FOR_CCJ(
        Claim::isEligibleForCCJ
    ),
    MORE_TIME_REQUESTED(
        Claim::hasResponseDeadlineBeenExtended
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
    CLAIMANT_ASKED_FOR_SETTLEMENT(
        Claim::hasClaimantAskedToSignSettlementAgreement
    ),
    PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE(
        Claim::isPassedToCountyCourtBusinessCentre
    ),
    CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT(
        Claim::hasClaimantAcceptedPartialAdmissionAmount
    ),
    SETTLEMENT_SIGNED(
        Claim::haveBothPartiesSignedSettlementAgreement
    ),
    NO_STATUS(c -> false);

    @Getter
    private final Predicate<Claim> claimMatcher;

    DashboardClaimStatus(Predicate<Claim> claimMatcher) {
        this.claimMatcher = claimMatcher;
    }
}
