package uk.gov.hmcts.reform.ucmc.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class ClaimValue {
    private final Integer lowerValue;
    private final Integer higherValue;

    public boolean hasLargerLowerValue() {
        if (lowerValue == null || higherValue == null) {
            return false;
        }

        return lowerValue > higherValue;
    }
}
