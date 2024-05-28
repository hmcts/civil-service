package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum PhysicalTrialBundleOptions {
    NONE("None"),
    PARTY("Party");

    private final String label;

    PhysicalTrialBundleOptions(String value) {
        this.label = value;
    }
}
