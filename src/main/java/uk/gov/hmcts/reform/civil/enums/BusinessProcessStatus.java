package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum BusinessProcessStatus {
    @CCD(label = "Business process ready")
    READY,
    @CCD(label = "Business process dispatched")
    DISPATCHED,
    @CCD(label = "Business process started")
    STARTED,
    @CCD(label = "Business process finished")
    FINISHED
}
