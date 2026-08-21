package uk.gov.hmcts.reform.civil.enums.dq;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "UnavailableDateChoice", generate = true)
public enum UnavailableDateType {
    @CCD(label = "Single date")
    SINGLE_DATE,
    @CCD(label = "Date range")
    DATE_RANGE;
}
