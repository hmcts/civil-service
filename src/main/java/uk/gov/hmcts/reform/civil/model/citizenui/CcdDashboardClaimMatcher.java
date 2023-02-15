package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Setter
public class CcdDashboardClaimMatcher implements Claim {

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);
    private CaseData caseData;

    @Override
    @JsonIgnore
    public boolean hasResponsePending() {
        return caseData.getRespondent1ResponseDate() == null;
    }

    @Override
    @JsonIgnore
    public boolean hasResponsePendingOverdue() {
        return caseData.getRespondent1ResponseDeadline() != null && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.hasBreathingSpace();
    }

    @Override
    public boolean hasResponseDueToday() {
        return caseData.getRespondent1ResponseDeadline() != null && caseData.getRespondent1ResponseDeadline().toLocalDate().isEqual(LocalDate.now())
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    public boolean hasResponseFullAdmit() {
        return caseData.getRespondent1ClaimResponseTypeForSpec() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION;
    }

    @Override
    @JsonIgnore
    public boolean defendantRespondedWithFullAdmitAndPayImmediately() {
        return hasResponseFullAdmit()
            && RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY == caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
    }

    @Override
    @JsonIgnore
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        return hasResponseFullAdmit()
            && caseData.isPayBySetDate();
    }

    @Override
    @JsonIgnore
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        return hasResponseFullAdmit()
            && caseData.isPayByInstallment();
    }

    @Override
    @JsonIgnore
    public boolean responseDeadlineHasBeenExtended() {
        return caseData.getRespondent1TimeExtensionDate() != null;
    }

    @Override
    @JsonIgnore
    public boolean isEligibleForCCJ() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(16, 1, 0));
    }

    @Override
    @JsonIgnore
    public boolean claimantConfirmedDefendantPaid() {
        return caseData.getRespondent1CourtOrderPayment() != null && caseData.respondent1PaidInFull();
    }

    @Override
    @JsonIgnore
    public boolean isSettled() {
        return caseData.respondent1PaidInFull() || caseData.isRepsonseAcceptedByClaimant();
    }

    @Override
    @JsonIgnore
    public boolean isSentToCourt() {
        return caseData.getCcdState() == CaseState.JUDICIAL_REFERRAL;
    }

    @Override
    @JsonIgnore
    public boolean claimantRequestedCountyCourtJudgement() {
        return caseData.getApplicant1DQ()!= null && caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() != null;
    }

}
