package uk.gov.hmcts.reform.civil.model.judgmentonline;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum JudgmentState {
    @CCD(label = "Pending issue")
    PENDING_ISSUE,
    @CCD(label = "Requested")
    REQUESTED,
    @CCD(label = "Issued")
    ISSUED,
    @CCD(label = "Modified")
    MODIFIED,
    @CCD(label = "Satisfied - Paid in Full")
    SATISFIED,
    @CCD(label = "Cancelled - Set aside")
    SET_ASIDE,
    @CCD(label = "Cancelled - set aside, judgment entered in error")
    SET_ASIDE_ERROR,
    @CCD(label = "Cancelled - Paid in Full")
    CANCELLED
}
