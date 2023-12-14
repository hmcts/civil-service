package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class RecurringIncomeLRspec {

    private final IncomeTypeLRspec type;
    private final String typeOtherDetails;
    /**
     * amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal amount;
    private final PaymentFrequencyLRspec frequency;
}
