package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum PaymentFrequencyClaimantResponseLRspec {
    @CCD(label = "Every week")
    ONCE_ONE_WEEK("Every week"),
    @CCD(label = "Every 2 weeks")
    ONCE_TWO_WEEKS("Every 2 weeks"),
    @CCD(label = "Every month")
    ONCE_ONE_MONTH("Every month");

    private final String label;
}
