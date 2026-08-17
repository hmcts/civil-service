package uk.gov.hmcts.reform.civil.enums.hearing;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum HearingChannel {
    @CCD(label = "In Person")
    IN_PERSON,
    @CCD(label = "Video")
    VIDEO,
    @CCD(label = "Telephone")
    TELEPHONE
}
