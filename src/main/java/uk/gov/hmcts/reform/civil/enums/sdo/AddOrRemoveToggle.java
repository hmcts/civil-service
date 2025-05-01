package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum AddOrRemoveToggle {

    ADD("Add/Remove");

    private final String label;

    AddOrRemoveToggle(String value) {
        this.label = value;
    }
}
