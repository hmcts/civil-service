package uk.gov.hmcts.reform.civil.enums.mediation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediationAppointmentTimeWindow {
    FROM_0800_TO_1000("0800-1000 AM"),
    FROM_0900_TO_1100("0900-1100 AM"),
    FROM_1000_TO_1200("1000-1200 AM"),
    FROM_1100_TO_1300("1100-1300 AM"),
    FROM_1200_TO_1400("1200-1400 PM"),
    FROM_1300_TO_1500("1300-1500 PM"),
    FROM_1400_TO_1600("1400-1600 PM"),
    FROM_1500_TO_1700("1500-1700 PM"),
    MORNING("0930-1230 AM"),
    AFTERNOON("1330-1630 PM");

    private final String displayedValue;
}
