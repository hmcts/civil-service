package uk.gov.hmcts.reform.civil.model.citizenui;

import uk.gov.hmcts.reform.civil.model.CaseData;

public class CcdClaimStatusDashboardBuilder extends DashboardClaimStatusBuilder<CaseData> {

    @Override
    public boolean hasResponsePending(CaseData claim) {
        return false;
    }

    @Override
    public boolean hasResponsePendingOverdue(CaseData claim) {
        return false;
    }

    @Override
    public boolean hasResponseDueToday(CaseData claim) {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately(CaseData claim) {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate(CaseData claim) {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments(CaseData claim) {
        return false;
    }

    @Override
    public boolean responseDeadlineHasBeenExtended(CaseData claim) {
        return false;
    }

    @Override
    public boolean isEligibleForCCJ(CaseData claim) {
        return false;
    }

    @Override
    public boolean claimantConfirmedDefendantPaid(CaseData claim) {
        return false;
    }

    @Override
    public boolean isSentToCourt(CaseData claim) {
        return false;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement(CaseData claim) {
        return false;
    }
}
