package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "WeekMonthPeriod", generate = true)
@Getter
@RequiredArgsConstructor
public enum PaymentFrequencyLRspec {
    @CCD(label = "Every week")
    ONCE_ONE_WEEK("Paid every week", "every week", "bob wythnos"),
    @CCD(label = "Every 2 weeks")
    ONCE_TWO_WEEKS("Paid every 2 weeks", "every 2 weeks", "bob pythefnos"),
    @CCD(label = "Every 3 weeks")
    ONCE_THREE_WEEKS("Paid every 3 weeks", "every 3 weeks", "bob tair wythnos"),
    @CCD(label = "Every 4 weeks")
    ONCE_FOUR_WEEKS("Paid every 4 weeks", "every 4 weeks", "bob pedair wythnos"),
    @CCD(label = "Every month")
    ONCE_ONE_MONTH("Paid every month", "every month", "bob mis");

    private final String label;
    private final String dashboardLabel;
    private final String dashboardLabelWelsh;
}
