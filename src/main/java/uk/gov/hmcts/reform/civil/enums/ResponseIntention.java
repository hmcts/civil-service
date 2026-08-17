package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum ResponseIntention {
    @CCD(label = "Defend all of the claim")
    FULL_DEFENCE("Defend all of the claim"),
    @CCD(label = "Defend part of the claim")
    PART_DEFENCE("Defend part of the claim"),
    @CCD(label = "Contest jurisdiction")
    CONTEST_JURISDICTION("Contest the Court's jurisdiction");

    private final String label;

    ResponseIntention(String value) {
        this.label = value;
    }
}
