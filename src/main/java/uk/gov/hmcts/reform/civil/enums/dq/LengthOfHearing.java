package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LengthOfHearing {
    MINUTES_15("15 minutes"),
    MINUTES_30("30 minutes"),
    HOUR_1("1 hour"),
    HOUR_1_5("1.5 hours"),
    HOURS_2("2 hours"),
    OTHER("Other");

    private final String displayedValue;
}
