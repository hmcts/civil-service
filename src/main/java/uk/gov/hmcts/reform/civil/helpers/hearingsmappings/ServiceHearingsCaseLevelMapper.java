package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ServiceHearingsCaseLevelMapper {

    private static String DATE_FORMAT = "yyyy-MM-dd";

    private ServiceHearingsCaseLevelMapper() {
        // no op
    }

    public static String getCaseSLAStartDate(LocalDate slaStartDate) {
        return slaStartDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
}
