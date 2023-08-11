package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class RepaymentPlanLRspec {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal paymentAmount;
    private final PaymentFrequencyLRspec repaymentFrequency;
    private final LocalDate firstRepaymentDate;

    @JsonIgnore
    public LocalDate finalPaymentBy(BigDecimal totalAmount) {
        if (firstRepaymentDate != null && paymentAmount != null && repaymentFrequency != null) {
            long installmentsAfterFirst = totalAmount.divide(MonetaryConversions.penniesToPounds(paymentAmount), 0, RoundingMode.CEILING)
                .longValue() - 1;
            switch (repaymentFrequency) {
                case ONCE_ONE_WEEK:
                    return firstRepaymentDate.plusWeeks(installmentsAfterFirst);
                case ONCE_TWO_WEEKS:
                    return firstRepaymentDate.plusWeeks(2 * installmentsAfterFirst);
                case ONCE_FOUR_WEEKS:
                    return firstRepaymentDate.plusWeeks(4 * installmentsAfterFirst);
                case ONCE_THREE_WEEKS:
                    return firstRepaymentDate.plusWeeks(3 * installmentsAfterFirst);
                default:
                    return firstRepaymentDate.plusMonths(installmentsAfterFirst);
            }
        }
        return null;
    }

    @JsonIgnore
    public String getPaymentFrequencyDisplay() {
        return repaymentFrequency.getLabel();
    }

}
