package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ClaimType {
    @CCD(label = "Personal injury")
    PERSONAL_INJURY(FeeType.LOWER),
    @CCD(label = "Clinical negligence")
    CLINICAL_NEGLIGENCE(FeeType.LOWER),
    @CCD(label = "Professional negligence")
    PROFESSIONAL_NEGLIGENCE(FeeType.LOWER),
    @CCD(label = "Breach of contract")
    BREACH_OF_CONTRACT(FeeType.HIGHER),
    @CCD(label = "Consumer")
    CONSUMER(FeeType.HIGHER),
    @CCD(label = "Consumer credit")
    CONSUMER_CREDIT(FeeType.HIGHER),
    @CCD(label = "Other")
    OTHER(FeeType.HIGHER),
    @CCD(label = "Flight delay")
    FLIGHT_DELAY(null),
    @CCD(label = "Damages and an ‘other’ remedy e.g. Payment Protection Insurance (PPI), Motor finance")
    DAMAGES_AND_OTHER_REMEDY(FeeType.HIGHER),
    @CCD(label = "Housing disrepair")
    HOUSING_DISREPAIR(FeeType.HIGHER);

    private final FeeType feeType;

    public enum FeeType {
        LOWER,
        HIGHER
    }
}
