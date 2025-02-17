package uk.gov.hmcts.reform.civil.enums;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.NOISE_INDUCED_HEARING_LOSS;

@Slf4j
public enum AllocatedTrack {
    SMALL_CLAIM,
    FAST_CLAIM,
    MULTI_CLAIM,
    INTERMEDIATE_CLAIM;

    public static AllocatedTrack getAllocatedTrack(BigDecimal statementOfValueInPounds, ClaimType claimType, PersonalInjuryType personalInjuryType,
                                                             FeatureToggleService featureToggleService, CaseData caseData) {
        Boolean intermediateOrMultiTrackValue = isValueGreaterThan(statementOfValueInPounds, 25000);
        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData) && intermediateOrMultiTrackValue.equals(true)) {
            log.info("isMultiOrIntermediateTrackEnabled toggle is on, for case {}, claim value {}",
                     caseData != null ? caseData.getCcdCaseReference() : "Unknown Case", statementOfValueInPounds);
            return isIntermediateOrMultiTrack(statementOfValueInPounds) ? INTERMEDIATE_CLAIM : MULTI_CLAIM;
        }
        return getAllocatedTrack(statementOfValueInPounds, claimType, personalInjuryType);
    }

    public static AllocatedTrack getAllocatedTrack(BigDecimal statementOfValueInPounds, ClaimType claimType, PersonalInjuryType personalInjuryType) {
        //The FLIGHT_DELAY ClaimType is only applicable for SPEC cases at the moment.
        if (claimType != null && claimType != ClaimType.FLIGHT_DELAY) {
            switch (claimType) {
                case PERSONAL_INJURY:
                    if (personalInjuryType != null && personalInjuryType.equals(NOISE_INDUCED_HEARING_LOSS)) {
                        return FAST_CLAIM;
                    } else {
                        return getAllocatedTrackForOtherPersonalInjuryAndClinicalNegligence(statementOfValueInPounds);
                    }
                case CLINICAL_NEGLIGENCE:
                    return getAllocatedTrackForOtherPersonalInjuryAndClinicalNegligence(statementOfValueInPounds);
                default:
                    return getAllocatedTrackForUnSpecDefault(statementOfValueInPounds);
            }
        } else { //For Spec Claims
            if (isValueSmallerThanOrEqualTo(statementOfValueInPounds, 10_000)) {
                return SMALL_CLAIM;
            } else if (isValueWithinRangeLowerExclusive(statementOfValueInPounds, 10_000, 25_000)) {
                return FAST_CLAIM;
            } else {
                return MULTI_CLAIM;
            }
        }
    }

    private static AllocatedTrack getAllocatedTrackForOtherPersonalInjuryAndClinicalNegligence(BigDecimal statementOfValueInPounds) {
        if (isValueSmallerThanOrEqualTo(statementOfValueInPounds, 1000)) {
            return SMALL_CLAIM;
        } else if (isBigDecimalValueWithinRange(statementOfValueInPounds, BigDecimal.valueOf(1000.01),
                                                BigDecimal.valueOf(25000)
        )) {
            return FAST_CLAIM;
        } else {
            return MULTI_CLAIM;
        }
    }

    private static AllocatedTrack getAllocatedTrackForUnSpecDefault(BigDecimal statementOfValueInPounds) {
        if (isValueSmallerThanOrEqualTo(statementOfValueInPounds, 10000)) {
            return SMALL_CLAIM;
        } else if (isBigDecimalValueWithinRange(statementOfValueInPounds, BigDecimal.valueOf(10000.01),
                                                BigDecimal.valueOf(25000)
        )) {
            return FAST_CLAIM;
        } else {
            return MULTI_CLAIM;
        }
    }

    public static int getDaysToAddToDeadlineSpec() {
        return 28;
    }

    private static boolean isValueSmallerThan(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) < 0;
    }

    private static boolean isValueGreaterThan(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) > 0;
    }

    private static boolean isValueSmallerThanOrEqualTo(BigDecimal value, int comparisionValue) {
        return value.compareTo(BigDecimal.valueOf(comparisionValue)) <= 0;
    }

    private static boolean isBigDecimalValueWithinRange(BigDecimal value, BigDecimal lower, BigDecimal higher) {
        return value.compareTo(lower) >= 0 &&  value.compareTo(higher) <= 0;
    }

    private static boolean isValueWithinRangeLowerExclusive(BigDecimal value, int lower, int higher) {
        return value.compareTo(BigDecimal.valueOf(lower)) > 0 &&  value.compareTo(BigDecimal.valueOf(higher)) <= 0;
    }

    public static String toStringValueForEmail(AllocatedTrack allocatedTrack) {
        switch (allocatedTrack) {
            case FAST_CLAIM:
                return "Fast Track";
            case MULTI_CLAIM:
                return "Multi Track";
            case SMALL_CLAIM:
                return "Small Claim Track";
            case INTERMEDIATE_CLAIM:
                return "Intermediate Track";
            default:
                throw new IllegalArgumentException("Invalid track type in " + allocatedTrack);
        }
    }

    public static String toStringValueForMessage(AllocatedTrack allocatedTrack) {
        switch (allocatedTrack) {
            case FAST_CLAIM:
                return "Fast track";
            case SMALL_CLAIM:
                return "Small claim";
            case MULTI_CLAIM:
                return "Multi track";
            case INTERMEDIATE_CLAIM:
                return "Intermediate track";
            default:
                throw new IllegalArgumentException("Invalid track type in " + allocatedTrack);
        }
    }

    private static boolean isIntermediateOrMultiTrack(BigDecimal statementOfValueInPounds) {
        return isValueSmallerThanOrEqualTo(statementOfValueInPounds, 100000);
    }
}
