package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.DebtTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

import java.math.BigDecimal;

@Data
@Builder
public class DebtLRspec {

    private final DebtTypeLRspec debtType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal paymentAmount;
    private final PaymentFrequencyLRspec paymentFrequency;

}
