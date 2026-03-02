package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimType {
    PERSONAL_INJURY(FeeType.LOWER),
    CLINICAL_NEGLIGENCE(FeeType.LOWER),
    PROFESSIONAL_NEGLIGENCE(FeeType.LOWER),
    BREACH_OF_CONTRACT(FeeType.HIGHER),
    CONSUMER(FeeType.HIGHER),
    CONSUMER_CREDIT(FeeType.HIGHER),
    OTHER(FeeType.HIGHER),
    FLIGHT_DELAY(null),
    DAMAGES_AND_OTHER_REMEDY(FeeType.HIGHER),
    HOUSING_DISREPAIR(FeeType.HIGHER);

    private final FeeType feeType;

    public enum FeeType {
        LOWER,
        HIGHER
    }
}
