package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum FeeType {
    HEARING("hearing"),
    CLAIMISSUED("claim");

    private final String label;

    FeeType(String value) {
        this.label = value;
    }
}
