package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum TrialOnRadioOptions {
    OPEN_DATE("First open date after"),
    TRIAL_WINDOW("Trial window");

    private final String label;

    TrialOnRadioOptions(String value) {
        this.label = value;
    }
}
