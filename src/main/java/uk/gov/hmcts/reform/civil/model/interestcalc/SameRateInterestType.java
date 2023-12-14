package uk.gov.hmcts.reform.civil.model.interestcalc;

public enum SameRateInterestType {
    SAME_RATE_INTEREST_8_PC("8%"),
    SAME_RATE_INTEREST_DIFFERENT_RATE("A different rate");

    final String description;

    SameRateInterestType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
