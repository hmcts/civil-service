package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
public class TimeConfiguration {

    @Bean
    @Lazy
    public Time currentTime() {
        return () -> ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime();
    }
}
