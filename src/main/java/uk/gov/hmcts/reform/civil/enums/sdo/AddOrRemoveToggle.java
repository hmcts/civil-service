package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum AddOrRemoveToggle {

    @CCD(label = "Add/Remove")
    ADD("Add/Remove");

    private final String label;

    AddOrRemoveToggle(String value) {
        this.label = value;
    }
}
