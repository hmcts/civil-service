package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum TrialOnRadioOptions {
    @CCD(label = "First open date after")
    OPEN_DATE("First open date after"),
    @CCD(label = "Trial window")
    TRIAL_WINDOW("Trial window");

    private final String label;

    TrialOnRadioOptions(String value) {
        this.label = value;
    }
}
