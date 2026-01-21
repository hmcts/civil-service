package uk.gov.hmcts.reform.civil.ga.helpers;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatHelper {

    public static final DateTimeFormatter JUDICIAL_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd",
                    Locale.ENGLISH);

    private DateFormatHelper() {
        //NO-OP
    }
}
