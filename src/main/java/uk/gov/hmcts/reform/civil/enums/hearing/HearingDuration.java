package uk.gov.hmcts.reform.civil.enums.hearing;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingLengthCasePro", generate = true)
public enum HearingDuration {
    @CCD(label = "5 minutes")
    MINUTES_05("5 minutes"),
    @CCD(label = "10 minutes")
    MINUTES_10("10 minutes"),
    @CCD(label = "15 minutes")
    MINUTES_15("15 minutes"),
    @CCD(label = "20 minutes")
    MINUTES_20("20 minutes"),
    @CCD(label = "25 minutes")
    MINUTES_25("25 minutes"),
    @CCD(label = "30 minutes")
    MINUTES_30("30 minutes"),
    @CCD(label = "35 minutes")
    MINUTES_35("35 minutes"),
    @CCD(label = "40 minutes")
    MINUTES_40("40 minutes"),
    @CCD(label = "45 minutes")
    MINUTES_45("45 minutes"),
    @CCD(label = "50 minutes")
    MINUTES_50("50 minutes"),
    @CCD(label = "55 minutes")
    MINUTES_55("55 minutes"),
    @CCD(label = "1 hour")
    MINUTES_60("1 hour"),
    @CCD(label = "1 and half hours")
    MINUTES_90("1 and a half hours"),
    @CCD(label = "2 hours")
    MINUTES_120("2 hours"),
    @CCD(label = "2 and half hours")
    MINUTES_150("2 and a half hours"),
    @CCD(label = "3 hours")
    MINUTES_180("3 hours"),
    @CCD(label = "4 hours")
    MINUTES_240("4 hours"),
    @CCD(label = "1 day")
    DAY_1("1 day"),
    @CCD(label = "2 days")
    DAY_2("2 days");

    private String label;

    HearingDuration(String value) {
        this.label = value;
    }
}
