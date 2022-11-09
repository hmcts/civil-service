package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum TrialHearingTimeEstimateDJ {

    ONE_HOUR("1 hour"),
    ONE_AND_HALF_HOUR("1.5 hours"),
    TWO_HOURS("2 hours"),
    THREE_HOURS("3 hours"),
    FOUR_HOURS("4 hours"),
    FIVE_HOURS("5 hours"),
    OTHER("Other");

    private final String label;

    TrialHearingTimeEstimateDJ(String value) {
        this.label = value;
    }
}
