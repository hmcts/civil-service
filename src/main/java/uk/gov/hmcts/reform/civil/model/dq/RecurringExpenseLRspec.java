package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
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
