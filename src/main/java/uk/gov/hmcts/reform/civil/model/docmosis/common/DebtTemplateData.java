package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DebtTemplateData {

    private String debtOwedTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal poundsOwed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal paidPerMonth;

    @JsonIgnore
    public static DebtTemplateData loanDebtFrom(LoanCardDebtLRspec debt) {
        return new DebtTemplateData()
            .setDebtOwedTo(debt.getLoanCardDebtDetail())
            .setPaidPerMonth((MonetaryConversions.penniesToPounds(debt.getMonthlyPayment())).setScale(2, RoundingMode.CEILING))
            .setPoundsOwed((MonetaryConversions.penniesToPounds(debt.getTotalOwed())).setScale(2, RoundingMode.CEILING));
    }

    @JsonIgnore
    public static DebtTemplateData generalDebtFrom(DebtLRspec debtLRspec) {
        DebtTemplateData form = new DebtTemplateData()
            .setDebtOwedTo(debtLRspec.getDebtType().getLabel());
        switch (debtLRspec.getPaymentFrequency()) {
            case ONCE_THREE_WEEKS:
                form.setPaidPerMonth(debtLRspec.getPaymentAmount() != null
                                         ? (MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount()
                                         .multiply(BigDecimal.valueOf(4))
                                         .divide(BigDecimal.valueOf(3), RoundingMode.CEILING)))
                                         .setScale(2, RoundingMode.CEILING) : new BigDecimal(0)
                    .setScale(2, RoundingMode.CEILING));
                break;
            case ONCE_TWO_WEEKS:
                form.setPaidPerMonth(debtLRspec.getPaymentAmount() != null
                                         ? (MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount().multiply(BigDecimal.valueOf(2))))
                                         .setScale(2, RoundingMode.CEILING) : new BigDecimal(0)
                    .setScale(2, RoundingMode.CEILING));
                break;
            case ONCE_ONE_WEEK:
                form.setPaidPerMonth(debtLRspec.getPaymentAmount() != null
                                         ? (MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount().multiply(BigDecimal.valueOf(4))))
                                         .setScale(2, RoundingMode.CEILING) : new BigDecimal(0)
                    .setScale(2, RoundingMode.CEILING));
                break;
            default:
                form.setPaidPerMonth(debtLRspec.getPaymentAmount() != null
                                         ? (MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount()))
                                         .setScale(2, RoundingMode.CEILING) : new BigDecimal(0)
                    .setScale(2, RoundingMode.CEILING));
                break;
        }
        return form;
    }
}
