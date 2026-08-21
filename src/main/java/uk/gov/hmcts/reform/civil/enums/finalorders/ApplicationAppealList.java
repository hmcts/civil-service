package uk.gov.hmcts.reform.civil.enums.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ApplicationAppealList {
    @CCD(label = "granted")
    GRANTED,
    @CCD(label = "refused")
    REFUSED,
    CIRCUIT_COURT,
    HIGH_COURT
}
