package uk.gov.hmcts.reform.civil.model.wa;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SystemDateProvider {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public String nowWithTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));

    }
}
