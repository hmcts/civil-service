package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum HearingOnRadioOptions {
    @CCD(label = "First open date after")
    OPEN_DATE("First open date after"),
    @CCD(label = "Hearing window")
    HEARING_WINDOW("Hearing window");

    private final String label;

    HearingOnRadioOptions(String value) {
        this.label = value;
    }
}
