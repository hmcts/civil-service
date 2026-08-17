package uk.gov.hmcts.reform.civil.model.interestcalc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SameRateInterestOptions", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SameRateInterestSelection {

    @CCD(label = " ", searchable = false)
    private SameRateInterestType sameRateInterestType;
    @CCD(
            label = "Rate you're claiming\n",
            showCondition = "sameRateInterestType = \"SAME_RATE_INTEREST_DIFFERENT_RATE\"",
            searchable = false
    )
    private BigDecimal differentRate;
    @CCD(
            label = "Why are you entitled to this rate?\n",
            showCondition = "sameRateInterestType = \"SAME_RATE_INTEREST_DIFFERENT_RATE\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String differentRateReason;

}
