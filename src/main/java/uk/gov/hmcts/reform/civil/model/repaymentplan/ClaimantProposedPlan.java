package uk.gov.hmcts.reform.civil.model.repaymentplan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
public class ClaimantProposedPlan {

    private RepaymentPlanLRspec repaymentPlanLRspec;
    private RespondentResponsePartAdmissionPaymentTimeLRspec proposedRepaymentType;
    private LocalDate repaymentByDate;
    private BigDecimal paymentAmount;

    @JsonIgnore
    public LocalDate getRepaymentDate(BigDecimal totalClaimAmount) {
        return Optional.ofNullable(repaymentPlanLRspec).map(repaymentPlan -> repaymentPlan.finalPaymentBy(totalClaimAmount))
            .orElse(repaymentByDate);
    }

}
