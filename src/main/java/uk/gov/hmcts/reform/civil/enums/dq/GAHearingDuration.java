package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAHearingDuration {
    MINUTES_15("15 minutes"),
    MINUTES_30("30 minutes"),
    MINUTES_45("45 minutes"),
    HOUR_1("1 hour"),
    HOURS_2("2 hours"),
    OTHER("Other");

    private final String displayedValue;
}
