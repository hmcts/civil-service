package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAHearingDuration {
    MINUTES_05("5 minutes"),
    MINUTES_10("10 minutes"),
    MINUTES_15("15 minutes"),
    MINUTES_20("20 minutes"),
    MINUTES_25("25 minutes"),
    MINUTES_30("30 minutes"),
    MINUTES_35("35 minutes"),
    MINUTES_40("40 minutes"),
    MINUTES_45("45 minutes"),
    MINUTES_50("50 minutes"),
    MINUTES_55("55 minutes"),
    HOUR_1("1 hour"),
    HOURS_2("2 hours"),
    OTHER("Other");

    private final String displayedValue;
}
