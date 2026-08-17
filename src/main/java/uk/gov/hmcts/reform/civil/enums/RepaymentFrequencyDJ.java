package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum RepaymentFrequencyDJ {
    @CCD(label = "Every week")
    ONCE_ONE_WEEK,
    @CCD(label = "Every 2 weeks")
    ONCE_TWO_WEEKS,
    @CCD(label = "Every month")
    ONCE_ONE_MONTH
}
