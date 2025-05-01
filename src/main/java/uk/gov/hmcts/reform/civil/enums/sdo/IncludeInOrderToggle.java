package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum IncludeInOrderToggle {

    INCLUDE("Include in Order");

    private final String label;

    IncludeInOrderToggle(String value) {
        this.label = value;
    }
}
