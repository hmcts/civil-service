package uk.gov.hmcts.reform.civil.model.citizenui;

import uk.gov.hmcts.reform.cmc.model.CmcClaim;

public class CmcStatusDashboardBuilder extends DashboardClaimStatusBuilder<CmcClaim> {

    @Override
    public boolean hasResponsePending(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean hasResponsePendingOverdue(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean hasResponseDueToday(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean responseDeadlineHasBeenExtended(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean isEligibleForCCJ(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean claimantConfirmedDefendantPaid(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean isSentToCourt(CmcClaim claim) {
        return false;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement(CmcClaim claim) {
        return false;
    }
}
