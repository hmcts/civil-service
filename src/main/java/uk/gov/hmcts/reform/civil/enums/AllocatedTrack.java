package uk.gov.hmcts.reform.civil.enums;

import java.math.BigDecimal;

public enum AllocatedTrack {
    SMALL_CLAIM,
    FAST_CLAIM,
    MULTI_CLAIM;

    public static AllocatedTrack getAllocatedTrack(BigDecimal statementOfValueInPounds, ClaimType claimType) {
        if (claimType != null && claimType.isLowerFeeType()) {
            if (isValueSmallerThan(statementOfValueInPounds, 1000)) {
                return SMALL_CLAIM;
            } else if (isValueWithinRange(statementOfValueInPounds, 1000, 25000)) {
                return FAST_CLAIM;
            }
        }

        if (isValueSmallerThan(statementOfValueInPounds, 10000)) {
            return SMALL_CLAIM;
        } else if (isValueWithinRange(statementOfValueInPounds, 10000, 25000)) {
            return FAST_CLAIM;
        } else {
            return MULTI_CLAIM;
        }
    }

    public static int getDaysToAddToDeadline(AllocatedTrack track) {
        if (track == SMALL_CLAIM) {
            return 14;
        }
        return 28;
    }

    public static int getDaysToAddToDeadlineSpec() {
        return 28;
    }

    private static boolean isValueSmallerThan(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) < 0;
    }

    private static boolean isValueWithinRange(BigDecimal value, int lower, int higher) {
        return value.compareTo(BigDecimal.valueOf(lower)) >= 0 &&  value.compareTo(BigDecimal.valueOf(higher)) <= 0;
    }

    public static String toStringValueForEmail(AllocatedTrack allocatedTrack) {
        switch (allocatedTrack) {
            case FAST_CLAIM:
                return "Fast Track";
            case MULTI_CLAIM:
                return "Multi Track";
            case SMALL_CLAIM:
                return "Small Claim Track";
            default:
                throw new IllegalArgumentException("Invalid track type in " + allocatedTrack);
        }
    }
}
