package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum MultiPartyResponseTypeFlags {
    @CCD(label = "Reject all of the claim")
    FULL_DEFENCE,
    @CCD(label = "Not Reject all of the claim")
    NOT_FULL_DEFENCE,
    @CCD(label = "Counter, admit or admit part")
    COUNTER_ADMIT_OR_ADMIT_PART,
    @CCD(label = "Admits all of the claim")
    FULL_ADMISSION,
    @CCD(label = "Admits part of the claim")
    PART_ADMISSION
}
