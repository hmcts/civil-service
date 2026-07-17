package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Slf4j
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentPlanLRspec {

    @CCD(
            label = "Regular payments of\n",
            hint = "For example, £50",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal paymentAmount;
    @CCD(label = "How often will your client make payments?", searchable = false)
    private PaymentFrequencyLRspec repaymentFrequency;
    @CCD(label = "When will your client make the first payment?", hint = "For example, 12 11 2007 ", searchable = false)
    private LocalDate firstRepaymentDate;

    @JsonIgnore
    public LocalDate finalPaymentBy(BigDecimal totalAmount) {
        if (firstRepaymentDate != null && paymentAmount != null && repaymentFrequency != null) {
            if (BigDecimal.ZERO.compareTo(paymentAmount) >= 0) {
                log.error("Payment amount is zero or less, cannot calculate final payment date");
                return null;
            }
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
