package uk.gov.hmcts.reform.civil.model.citizenui;

public interface Claim {

    boolean hasResponsePending();

    boolean hasResponsePendingOverdue();

    boolean hasResponseDueToday();

    boolean hasResponseFullAdmit();

    boolean defendantRespondedWithFullAdmitAndPayImmediately();

    boolean defendantRespondedWithFullAdmitAndPayBySetDate();

    boolean defendantRespondedWithFullAdmitAndPayByInstallments();

    boolean responseDeadlineHasBeenExtended();

    boolean isEligibleForCCJ();

    boolean claimantConfirmedDefendantPaid();

    boolean isSettled();

    boolean isSentToCourt();

    boolean claimantRequestedCountyCourtJudgement();

}
