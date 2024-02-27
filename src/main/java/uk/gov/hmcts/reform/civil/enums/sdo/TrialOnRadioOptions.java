package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum TrialOnRadioOptions {
    OPEN_DATE("First open date after"),
    HEARING_WINDOW("Hearing window");

    private final String label;

    TrialOnRadioOptions(String value) {
        this.label = value;
    }
}
