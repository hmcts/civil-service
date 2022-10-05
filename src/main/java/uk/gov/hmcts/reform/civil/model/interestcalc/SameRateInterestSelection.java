package uk.gov.hmcts.reform.civil.model.interestcalc;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SameRateInterestSelection {

    private final SameRateInterestType sameRateInterestType;
    private final BigDecimal differentRate;
    private final String differentRateReason;

}
