package uk.gov.hmcts.reform.civil.model.interestcalc;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "InterestOptions", generate = true)
public enum InterestClaimOptions {
    @CCD(label = "Same rate for whole period of time")
    SAME_RATE_INTEREST("Same rate for whole period of time"),
    @CCD(
            label = "Break down interest for different periods of time, or items. \n\n\n You can only use this service if any claim for interest is made at the same rate and from the same date. To claim interest at different rates or for different periods of time, you should issue your claim on paper"
    )
    BREAK_DOWN_INTEREST("Break down interest for different periods of time, or items");

    String description;

    InterestClaimOptions(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
