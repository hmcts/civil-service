package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum PhysicalTrialBundleOptions {
    @CCD(label = "None")
    NONE("None"),
    @CCD(label = "Party")
    PARTY("Party");

    private final String label;

    PhysicalTrialBundleOptions(String value) {
        this.label = value;
    }
}
