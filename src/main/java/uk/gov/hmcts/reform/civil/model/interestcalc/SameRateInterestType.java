package uk.gov.hmcts.reform.civil.model.interestcalc;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SameRateInterestSelectionList", generate = true)
public enum SameRateInterestType {
    @CCD(label = "8%")
    SAME_RATE_INTEREST_8_PC("8%"),
    @CCD(label = "A different rate")
    SAME_RATE_INTEREST_DIFFERENT_RATE("A different rate");

    String description;

    SameRateInterestType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
