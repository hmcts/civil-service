package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@Data
public class DebtTemplateData {

    private final String debtOwedTo;
    private final BigDecimal poundsOwed;
    private final BigDecimal paidPerMonth;

    @JsonIgnore
    public static DebtTemplateData loanDebtFrom(final LoanCardDebtLRspec debt) {
        return DebtTemplateData.builder()
            .debtOwedTo(debt.getLoanCardDebtDetail())
            .paidPerMonth(MonetaryConversions.penniesToPounds(debt.getMonthlyPayment()))
            .poundsOwed(MonetaryConversions.penniesToPounds(debt.getTotalOwed()))
            .build();
    }

    @JsonIgnore
    public static DebtTemplateData generalDebtFrom(final DebtLRspec debtLRspec) {
        DebtTemplateData.DebtTemplateDataBuilder builder = DebtTemplateData.builder()
            .debtOwedTo(debtLRspec.getDebtType().getLabel());
        switch (debtLRspec.getPaymentFrequency()) {
            case ONCE_THREE_WEEKS:
                builder.paidPerMonth(MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount()
                                         .multiply(BigDecimal.valueOf(4))
                                         .divide(BigDecimal.valueOf(3), RoundingMode.CEILING)));
                break;
            case ONCE_TWO_WEEKS:
                builder.paidPerMonth(MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount().multiply(BigDecimal.valueOf(2))));
                break;
            case ONCE_ONE_WEEK:
                builder.paidPerMonth(MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount().multiply(BigDecimal.valueOf(4))));
                break;
            default:
                builder.paidPerMonth(MonetaryConversions.penniesToPounds(debtLRspec.getPaymentAmount()));
                break;
        }
        return builder.build();
    }
}
