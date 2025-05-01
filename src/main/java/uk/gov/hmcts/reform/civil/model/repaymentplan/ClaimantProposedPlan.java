package uk.gov.hmcts.reform.civil.model.repaymentplan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;
import static uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator.calculatePaymentPerMonth;

@Data
@Builder
public class ClaimantProposedPlan {

    private RepaymentPlanLRspec repaymentPlanLRspec;
    private RespondentResponsePartAdmissionPaymentTimeLRspec proposedRepaymentType;
    private LocalDate repaymentByDate;

    @JsonIgnore
    public LocalDate getRepaymentDate(BigDecimal totalClaimAmount) {
        return Optional.ofNullable(repaymentPlanLRspec).map(repaymentPlan -> repaymentPlan.finalPaymentBy(totalClaimAmount))
            .orElse(repaymentByDate);
    }

    @JsonIgnore
    public double getCalculatedPaymentPerMonthFromRepaymentPlan() {
        return calculatePaymentPerMonth(
            penniesToPounds(repaymentPlanLRspec.getPaymentAmount()).doubleValue(),
            repaymentPlanLRspec.getRepaymentFrequency()
        );
    }

    @JsonIgnore
    public boolean hasProposedPayImmediately() {
        return IMMEDIATELY == proposedRepaymentType;
    }

    @JsonIgnore
    public boolean hasProposedPayBySetDate() {
        return BY_SET_DATE == proposedRepaymentType;
    }

    @JsonIgnore
    public boolean hasProposedPayByInstallments() {
        return SUGGESTION_OF_REPAYMENT_PLAN == proposedRepaymentType;
    }

}
