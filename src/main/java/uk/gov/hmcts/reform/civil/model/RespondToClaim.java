package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RespondToClaim {

    private final BigDecimal howMuchWasPaid;
    private final LocalDate whenWasThisAmountPaid;
    private final String howWasThisAmountPaid;

}
