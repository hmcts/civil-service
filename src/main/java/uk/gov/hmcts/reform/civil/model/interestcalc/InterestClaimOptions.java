package uk.gov.hmcts.reform.civil.model.interestcalc;

public enum InterestClaimOptions {
    SAME_RATE_INTEREST("Same rate for whole period of time"),
    BREAK_DOWN_INTEREST("Break down interest for different periods of time, or items");

    final String description;

    InterestClaimOptions(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
