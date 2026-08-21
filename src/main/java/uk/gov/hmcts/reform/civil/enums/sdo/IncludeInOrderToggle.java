package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum IncludeInOrderToggle {

    @CCD(label = "Include in Order")
    INCLUDE("Include in Order");

    private final String label;

    IncludeInOrderToggle(String value) {
        this.label = value;
    }
}
