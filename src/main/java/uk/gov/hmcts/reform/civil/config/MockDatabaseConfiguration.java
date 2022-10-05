package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.repositories.HearingReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;

@Configuration
@ConditionalOnProperty(value = "reference.database.enabled", havingValue = "false")
public class MockDatabaseConfiguration {

    @Bean
    public ReferenceNumberRepository referenceNumberRepository() {
        return () -> "000DC001";
    }

    @Bean
    public SpecReferenceNumberRepository specReferenceNumberRepository() {
        return () -> "000MC001";
    }

    @Bean
    public HearingReferenceNumberRepository hearingReferenceNumberRepository() {
        return () -> "000HN001";
    }
}
