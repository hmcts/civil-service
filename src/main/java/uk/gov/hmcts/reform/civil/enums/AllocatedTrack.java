package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.NOISE_INDUCED_HEARING_LOSS;

public enum AllocatedTrack {
    SMALL_CLAIM,
    FAST_CLAIM,
    MULTI_CLAIM;

    public static AllocatedTrack getAllocatedTrack(CaseData caseData) {
        BigDecimal statementOfValueInPounds;
        boolean noClaimValue;
        if (caseData.getClaimValue() != null) {
            statementOfValueInPounds = caseData.getClaimValue().toPounds();
            noClaimValue = false;
        } else {
            statementOfValueInPounds = null;
            noClaimValue = true;
        }

        if (caseData.getClaimType() != null) {
            ClaimType claimType = caseData.getClaimType();
            if (claimType == ClaimType.PERSONAL_INJURY || claimType == ClaimType.CLINICAL_NEGLIGENCE) {
                if (caseData.getPersonalInjuryType() != null
                    && (caseData.getPersonalInjuryType().equals(NOISE_INDUCED_HEARING_LOSS))) {
                    return FAST_CLAIM;
                }

                if (!noClaimValue && isValueSmallerThanOrEqualTo(statementOfValueInPounds, 1000)) {
                    return SMALL_CLAIM;
                } else if (!noClaimValue && isBigDecimalValueWithinRange(statementOfValueInPounds, BigDecimal.valueOf(1000.01),
                        BigDecimal.valueOf(25000))) {
                    return FAST_CLAIM;
                } else {
                    return MULTI_CLAIM;
                }
            }

            if (!noClaimValue && isValueSmallerThanOrEqualTo(statementOfValueInPounds, 10000)) {
                return SMALL_CLAIM;
            } else if (!noClaimValue && isBigDecimalValueWithinRange(statementOfValueInPounds, BigDecimal.valueOf(10000.01),
                    BigDecimal.valueOf(25000))) {
                return FAST_CLAIM;
            } else {
                return MULTI_CLAIM;
            }

        } else { //For Spec Claims
            if (!noClaimValue && isValueSmallerThan(statementOfValueInPounds, 10000)) {
                return SMALL_CLAIM;
            } else if (!noClaimValue && isValueWithinRange(statementOfValueInPounds, 10000, 25000)) {
                return FAST_CLAIM;
            } else {
                return MULTI_CLAIM;
            }
        }
    }

    public static int getDaysToAddToDeadline(AllocatedTrack track) {
        if (track == SMALL_CLAIM) {
            return 14;
        }
        return 28;
    }

    public static int getDaysToAddToDeadlineSpec(AllocatedTrack track) {
        return 28;
    }

    private static boolean isValueSmallerThan(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) < 0;
    }

    private static boolean isValueSmallerThanOrEqualTo(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) <= 0;
    }

    private static boolean isBigDecimalValueWithinRange(BigDecimal value, BigDecimal lower, BigDecimal higher) {
        return value.compareTo(lower) >= 0 &&  value.compareTo(higher) <= 0;
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
