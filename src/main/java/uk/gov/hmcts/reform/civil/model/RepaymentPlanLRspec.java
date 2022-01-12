package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class RepaymentPlanLRspec {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal paymentAmount;
    private final PaymentFrequencyLRspec repaymentFrequency;
    private final LocalDate firstRepaymentDate;
    private int lengthOfPaymentPlan;

}
