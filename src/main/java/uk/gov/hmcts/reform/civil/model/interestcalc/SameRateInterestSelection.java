package uk.gov.hmcts.reform.civil.model.interestcalc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SameRateInterestSelection {

    private SameRateInterestType sameRateInterestType;
    private BigDecimal differentRate;
    private String differentRateReason;

}
