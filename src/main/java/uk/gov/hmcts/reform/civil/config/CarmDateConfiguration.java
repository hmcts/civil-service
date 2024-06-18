package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Configuration
public class CarmDateConfiguration {

    private final LocalDate carmDate;

    public CarmDateConfiguration(@Value("${carmDate}") String carmDate) {
        this.carmDate = LocalDate.parse(carmDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
