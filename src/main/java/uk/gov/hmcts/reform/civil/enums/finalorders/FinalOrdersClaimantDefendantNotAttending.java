package uk.gov.hmcts.reform.civil.enums.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum FinalOrdersClaimantDefendantNotAttending {
    @CCD(label = "Satisfied reasonable to proceed")
    SATISFIED_REASONABLE_TO_PROCEED,
    @CCD(label = "Satisfied notice of trial received, not reasonable to proceed")
    SATISFIED_NOTICE_OF_TRIAL,
    @CCD(label = "Not satisfied notice of trial received, not reasonable to proceed")
    NOT_SATISFIED_NOTICE_OF_TRIAL
}
