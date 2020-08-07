package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.unspec.utils.MonetaryConversions;

import java.math.BigDecimal;

@Data
@Builder
@RequiredArgsConstructor
public class ClaimValue {

    private final BigDecimal lowerValue;
    private final BigDecimal higherValue;

    public boolean hasLargerLowerValue() {
        if (lowerValue == null || higherValue == null) {
            return false;
        }

        return lowerValue.compareTo(higherValue) > 0;
    }

    public String formData() {
        BigDecimal higherAmount = MonetaryConversions.penniesToPounds(higherValue);
        if (lowerValue == null) {
            return "up to £" + higherAmount;
        }
        return "£" + MonetaryConversions.penniesToPounds(lowerValue) + " - £" + higherAmount;
    }
}
