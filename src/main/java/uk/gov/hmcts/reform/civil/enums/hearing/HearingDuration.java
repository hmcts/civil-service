package uk.gov.hmcts.reform.civil.enums.hearing;

public enum HearingDuration {
    MINUTES_30("30 minutes"),
    MINUTES_60("1 hour"),
    MINUTES_90("1 and a half hours"),
    MINUTES_120("2 hours"),
    MINUTES_150("2 and a half hours"),
    MINUTES_180("3 hours"),
    MINUTES_240("4 hours"),
    DAY_1("1 day"),
    DAY_2("2 days");

    private String label;

    HearingDuration(String value) {
        this.label = value;
    }
}
