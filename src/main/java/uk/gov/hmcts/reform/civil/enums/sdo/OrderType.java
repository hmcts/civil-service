package uk.gov.hmcts.reform.civil.enums.sdo;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum OrderType {
    @CCD(label = "Disposal hearing")
    DISPOSAL,
    @CCD(label = "A trial to decide the amount of damages")
    DECIDE_DAMAGES
}
