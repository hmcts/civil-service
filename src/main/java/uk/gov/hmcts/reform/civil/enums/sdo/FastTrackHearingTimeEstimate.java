package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FastTrackTimeEstimate", generate = true)
@Getter
public enum FastTrackHearingTimeEstimate {

    @CCD(label = "1 hour")
    ONE_HOUR("1 hour"),
    @CCD(label = "1.5 hours")
    ONE_AND_HALF_HOUR("1.5 hours"),
    @CCD(label = "2 hours")
    TWO_HOURS("2 hours"),
    @CCD(label = "3 hours")
    THREE_HOURS("3 hours"),
    @CCD(label = "4 hours")
    FOUR_HOURS("4 hours"),
    @CCD(label = "5 hours")
    FIVE_HOURS("5 hours"),
    @CCD(label = "Other")
    OTHER("Other");

    private final String label;

    FastTrackHearingTimeEstimate(String value) {
        this.label = value;
    }
}
