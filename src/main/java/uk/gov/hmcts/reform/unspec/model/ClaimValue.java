package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
}
