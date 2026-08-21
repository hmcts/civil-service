package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum SmallClaimsMethodTelephoneHearing {
    @CCD(label = "The claimant")
    @JsonProperty("telephoneTheClaimant")
    TELEPHONE_THE_CLAIMANT("the claimant"),
    @CCD(label = "The defendant")
    @JsonProperty("telephoneTheDefendant")
    TELEPHONE_THE_DEFENDANT("the defendant"),
    @CCD(label = "The court")
    @JsonProperty("telephoneTheCourt")
    TELEPHONE_THE_COURT("the court");

    private final String label;

    SmallClaimsMethodTelephoneHearing(String value) {
        this.label = value;
    }
}
