package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class RecurringExpenseLRspec {

    private ExpenseTypeLRspec type;
    private String typeOtherDetails;
    /**
     * amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;
    private PaymentFrequencyLRspec frequency;
}
