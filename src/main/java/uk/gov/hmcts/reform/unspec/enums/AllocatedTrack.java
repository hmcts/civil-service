package uk.gov.hmcts.reform.unspec.enums;

import uk.gov.hmcts.reform.unspec.model.ClaimValue;

import java.math.BigDecimal;

public enum AllocatedTrack {
    SMALL_CLAIM,
    FAST_CLAIM,
    MULTI_CLAIM;

    public static AllocatedTrack getAllocatedTrack(ClaimValue claimValue, ClaimType claimType) {
        if (claimType.isPersonalInjury()) {
            if (isValueSmallerThan(claimValue.getHigherValue(), 1000)) {
                return SMALL_CLAIM;
            } else if (isValueWithinRange(claimValue.getHigherValue(), 1000, 25000)) {
                return FAST_CLAIM;
            }
        }

        if (isValueSmallerThan(claimValue.getHigherValue(), 10000)) {
            return SMALL_CLAIM;
        } else if (isValueWithinRange(claimValue.getHigherValue(), 10000, 25000)) {
            return FAST_CLAIM;
        } else {
            return MULTI_CLAIM;
        }
    }

    private static boolean isValueSmallerThan(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) < 0;
    }

    private static boolean isValueWithinRange(BigDecimal value, int lower, int higher) {
        return value.compareTo(BigDecimal.valueOf(lower)) >= 0 &&  value.compareTo(BigDecimal.valueOf(higher)) <= 0;
    }
}
