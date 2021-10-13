package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder
public class RespondToClaim {

    private final BigDecimal howMuchWasPaid;
    @PastOrPresent(message = "Date for when amount was paid must be today or in the past",
        groups = PaymentDateGroup.class)
    private final LocalDate whenWasThisAmountPaid;
    private final PaymentMethod howWasThisAmountPaid;
    private final String howWasThisAmountPaidOther;

}
