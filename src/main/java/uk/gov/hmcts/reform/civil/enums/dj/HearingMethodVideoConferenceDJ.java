package uk.gov.hmcts.reform.civil.enums.dj;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DisposalHearingMethodVideoConferenceHearingDJ", generate = true)
@Getter
public enum HearingMethodVideoConferenceDJ {
    @CCD(label = "The claimant")
    @JsonProperty("videoTheClaimant")
    VIDEO_THE_CLAIMANT("the claimant"),

    @CCD(label = "The defendant")
    @JsonProperty("videoTheDefendant")
    VIDEO_THE_DEFENDANT("the defendant"),

    @CCD(label = "The court")
    @JsonProperty("videoTheCourt")
    VIDEO_THE_COURT("the court");

    private final String label;

    HearingMethodVideoConferenceDJ(String value) {
        this.label = value;
    }

}
