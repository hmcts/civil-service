package uk.gov.hmcts.reform.civil.model.citizenui;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CcdClaimStatusDashboardFactory extends DashboardClaimStatusFactory<CaseData> {

    @Override
    public boolean hasResponsePending(CaseData claim) {
        return claim.getRespondent1ResponseDate() == null;
    }

    @Override
    public boolean hasResponsePendingOverdue(CaseData claim) {
        return claim.getRespondent1ResponseDeadline() != null && claim.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(16, 1, 0))
            && claim.hasBreathingSpace();
    }

    @Override
    public boolean hasResponseDueToday(CaseData claim) {
        return claim.getRespondent1ResponseDeadline() != null && claim.getRespondent1ResponseDeadline().isEqual(LocalDateTime.now())
            && claim.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(16, 1, 0));
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately(CaseData claim) {
        return claim.isResponseFullAdmitAndPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate(CaseData claim) {
        return claim.isResponseFullAdmitAndPayBySetDate();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments(CaseData claim) {
        return claim.isResponseFullAdmitAndPayByInstallments();
    }

    @Override
    public boolean responseDeadlineHasBeenExtended(CaseData claim) {
        return claim.getRespondent1TimeExtensionDate() != null;
    }

    @Override
    public boolean isEligibleForCCJ(CaseData claim) {
        return claim.getRespondent1ResponseDeadline() != null
            && claim.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(16, 1, 0));
    }

    @Override
    public boolean claimantConfirmedDefendantPaid(CaseData claim) {
        return claim.getRespondent1CourtOrderPayment() != null && claim.respondent1PaidInFull();
    }

    @Override
    public boolean claimIsSettled(CaseData claim) {
        return claim.respondent1PaidInFull() || claim.isRepsonseAcceptedByClaimant();
    }

    @Override
    public boolean isSentToCourt(CaseData claim) {
        return claim.getCcdState() == CaseState.JUDICIAL_REFERRAL;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement(CaseData claim) {
        return claim.getApplicant1DQ().getApplicant1DQRequestedCourt() != null;
    }

}
