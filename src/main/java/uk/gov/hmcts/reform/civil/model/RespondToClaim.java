package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import javax.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RespondToClaim {

    private final BigDecimal howMuchWasPaid;
    @PastOrPresent(message = "The date entered cannot be in the future", groups = PaymentDateGroup.class)
    private final LocalDate whenWasThisAmountPaid;
    private final PaymentMethod howWasThisAmountPaid;
    private final String howWasThisAmountPaidOther;

}
