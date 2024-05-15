package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum SmallClaimsSdoR2PhysicalTrialBundleOptions {
    NO("No"),
    PARTY("Party");

    private final String label;

    SmallClaimsSdoR2PhysicalTrialBundleOptions(String value) {
        this.label = value;
    }
}
