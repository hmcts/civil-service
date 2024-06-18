package uk.gov.hmcts.reform.civil.enums.hearing;

public enum HearingDuration {
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
