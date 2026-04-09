package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.DebtTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class DebtLRspec {

    private DebtTypeLRspec debtType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal paymentAmount;
    private PaymentFrequencyLRspec paymentFrequency;

}
