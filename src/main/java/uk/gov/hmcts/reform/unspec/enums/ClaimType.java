package uk.gov.hmcts.reform.unspec.enums;

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
    OTHER(FeeType.HIGHER);

    private final FeeType feeType;

    public enum FeeType {
        LOWER,
        HIGHER
    }

    public boolean isLowerFeeType() {
        return this.feeType.equals(FeeType.LOWER);
    }
}
