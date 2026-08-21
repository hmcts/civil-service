package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum SmallClaimsSdoR2PhysicalTrialBundleOptions {
    @CCD(label = "No")
    NO("No"),
    @CCD(label = "Party")
    PARTY("Party");

    private final String label;

    SmallClaimsSdoR2PhysicalTrialBundleOptions(String value) {
        this.label = value;
    }
}
