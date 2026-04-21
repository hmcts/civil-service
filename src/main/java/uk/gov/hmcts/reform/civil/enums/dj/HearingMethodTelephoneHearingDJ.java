package uk.gov.hmcts.reform.civil.enums.dj;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum HearingMethodTelephoneHearingDJ {
    @JsonProperty("telephoneTheClaimant")
    TELEPHONE_THE_CLAIMANT("the claimant"),

    @JsonProperty("telephoneTheDefendant")
    TELEPHONE_THE_DEFENDANT("the defendant"),

    @JsonProperty("telephoneTheCourt")
    TELEPHONE_THE_COURT("the court");

    private final String label;

    HearingMethodTelephoneHearingDJ(String value) {
        this.label = value;
    }

}
