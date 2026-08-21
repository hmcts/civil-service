package uk.gov.hmcts.reform.civil.model.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum OrderAfterHearingDateType {
    @CCD(label = "Single date")
    SINGLE_DATE,
    @CCD(label = "Date range")
    DATE_RANGE,
    @CCD(label = "Bespoke range")
    BESPOKE_RANGE
}
