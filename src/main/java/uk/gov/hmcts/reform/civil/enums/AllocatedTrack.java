package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.NOISE_INDUCED_HEARING_LOSS;

public enum AllocatedTrack {
    SMALL_CLAIM,
    FAST_CLAIM,
    MULTI_CLAIM;

    public static AllocatedTrack getAllocatedTrack(CaseData caseData) {
        //The FLIGHT_DELAY ClaimType is only applicable for SPEC cases at the moment.
        if (caseData.getClaimType() != null && caseData.getClaimType() != ClaimType.FLIGHT_DELAY) {
            ClaimType claimType = caseData.getClaimType();
            if (claimType == ClaimType.PERSONAL_INJURY || claimType == ClaimType.CLINICAL_NEGLIGENCE) {
                return getAllocatedTrackForUnSpecPersonalInjuryOrClinicalNegligence(caseData);
            }
            return getAllocatedTrackForUnSpecDefault(caseData);
        } else { //For Spec Claims
            return getAllocatedTrackForSpec(caseData);
        }
    }

    private static AllocatedTrack getAllocatedTrackForUnSpecPersonalInjuryOrClinicalNegligence(CaseData caseData) {
        BigDecimal statementOfValueInPounds;
        if (caseData.getPersonalInjuryType() != null
            && (caseData.getPersonalInjuryType().equals(NOISE_INDUCED_HEARING_LOSS))) {
            return FAST_CLAIM;
        }
        if (caseData.getClaimValue() != null) {
            statementOfValueInPounds = caseData.getClaimValue().toPounds();
            if (isValueSmallerThanOrEqualTo(statementOfValueInPounds, 1000)) {
                return SMALL_CLAIM;
            } else if (isBigDecimalValueWithinRange(
                statementOfValueInPounds,
                BigDecimal.valueOf(1000.01),
                BigDecimal.valueOf(25000)
            )) {
                return FAST_CLAIM;
            }
        }
        return MULTI_CLAIM;
    }

    private static AllocatedTrack getAllocatedTrackForUnSpecDefault(CaseData caseData) {
        BigDecimal statementOfValueInPounds;
        if (caseData.getClaimValue() != null) {
            statementOfValueInPounds = caseData.getClaimValue().toPounds();
            if (isValueSmallerThanOrEqualTo(statementOfValueInPounds, 10000)) {
                return SMALL_CLAIM;
            } else if (isBigDecimalValueWithinRange(
                statementOfValueInPounds,
                BigDecimal.valueOf(10000.01),
                BigDecimal.valueOf(25000)
            )) {
                return FAST_CLAIM;
            }
        }
        return MULTI_CLAIM;
    }

    private static AllocatedTrack getAllocatedTrackForSpec(CaseData caseData) {
        BigDecimal statementOfValueInPounds;
        if (caseData.getTotalClaimAmount() != null) {
            statementOfValueInPounds = caseData.getTotalClaimAmount();
            if (isValueSmallerThan(statementOfValueInPounds, 10000)) {
                return SMALL_CLAIM;
            } else if (isValueWithinRange(statementOfValueInPounds, 10000, 25000)) {
                return FAST_CLAIM;
            }
        }
        return MULTI_CLAIM;
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
